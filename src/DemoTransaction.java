import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DemoTransaction {
    public static void main(String[] args) throws SQLException {
        // 开机恢复
        Set<UpdateBalanceCommand> undoBalanceUpdateCommands = DemoAtomicity.getUpdateLogsWithProblem();
        for (Command1 cmd : undoBalanceUpdateCommands) {
            cmd.undo();
        }

        /*
        Map<Integer, BankAccount> allBankAccounts = DemoAtomicity.getAllBankAccount();
        // Alice 转账50.00 给Bob, 顺利完成
        DemoAtomicity.transferMoney(allBankAccounts.get(1), allBankAccounts.get(2), 50.0f);
        // Alice 转账100.00 给Bob, 服务器中途挂了
        DemoAtomicity.transferMoneyHalfWay(allBankAccounts.get(1), allBankAccounts.get(2), 100.0f);
        */
        System.out.println();
    }
}

class DemoAtomicity {
    private static int transferID = 1;
    public static Map<Integer, BankAccount> getAllBankAccount() throws SQLException {
        ResultSet resultSet = DemoAtomicity.readFromDB("SELECT * FROM BankAccounts;");
        MyORM myORM = new MyORM(resultSet);
        return myORM.toBankAccount();
    }

    public static Set<UpdateBalanceCommand> getUpdateLogsWithProblem() throws SQLException {
        Map<Integer, BankAccount> allBankAccounts =  DemoAtomicity.getAllBankAccount();
        Set<UpdateBalanceCommand> rets = new HashSet<>();
        ResultSet tranLogsWithProblem = DemoAtomicity.readFromDB("SELECT * FROM TransactionLog \n" +
                "WHERE tran_log_id in (\n" +
                "SELECT t.tran_log_id FROM TransactionLog t, balanceupdatelog b\n" +
                "WHERE b.update_log_id = t.src_account_update_log_id OR b.update_log_id = t.dest_account_update_log_id\n" +
                "GROUP BY t.tran_log_id\n" +
                "HAVING COUNT(*) != 2\n" +
                ");");

        while (tranLogsWithProblem.next()) {
            int tran_log_id = (int) tranLogsWithProblem.getObject("tran_log_id");
            try {
                int src_account_update_log_id = (int) tranLogsWithProblem.getObject("src_account_update_log_id");
                ResultSet src_account_update_log = DemoAtomicity.readFromDB("SELECT * FROM BalanceUpdateLog WHERE update_log_id = " + src_account_update_log_id + ";");
                rets.addAll(new MyORM(src_account_update_log).toUpdateBalanceCommand(allBankAccounts, tran_log_id));
            } catch (Exception ignored) {

            }


            try {
                int dest_account_update_log_id = (int) tranLogsWithProblem.getObject("dest_account_update_log_id");
                ResultSet dest_account_update_log = DemoAtomicity.readFromDB("SELECT * FROM BalanceUpdateLog WHERE update_log_id = " + dest_account_update_log_id + ";");
                rets.addAll(new MyORM(dest_account_update_log).toUpdateBalanceCommand(allBankAccounts, tran_log_id));
            } catch (Exception ignored) {

            }
        }

        return rets;

    }

    private static ResultSet readFromDB(String query) throws SQLException {
        DBConn dbConn = new DBConn("localhost:3306", "DemoTransaction", "myusername", "123456");
        dbConn.readQuery(query);
        return dbConn.getResultSet();
    }


    // 转账顺利完成的情况
    public static void transferMoney(BankAccount srcAcc, BankAccount destAcc, float amount) throws  SQLException {
        DBConn dbConn = new DBConn("localhost:3306", "DemoTransaction", "myusername", "123456");
        dbConn.nonReadQuery("INSERT INTO TransactionLog (`tran_log_id`) VALUES (" + DemoAtomicity.transferID + ");");
        Command1 decreSrcAccCmd = new UpdateBalanceCommand(srcAcc, -amount, DemoAtomicity.transferID);
        Command1 increDestAccCmd = new UpdateBalanceCommand(destAcc, amount, DemoAtomicity.transferID);

        decreSrcAccCmd.execute();
        increDestAccCmd.execute();

        DemoAtomicity.transferID++;
    }

    // 模拟服务器在执行decreSrcAccCmd.execute()之后挂了，srcAcc的balance减少了，但是destAcc的balance还未增加!
    public static void transferMoneyHalfWay(BankAccount srcAcc, BankAccount destAcc, float amount) throws  SQLException {
        DBConn dbConn = new DBConn("localhost:3306", "DemoTransaction", "myusername", "123456");
        dbConn.nonReadQuery("INSERT INTO TransactionLog (`tran_log_id`) VALUES (" + DemoAtomicity.transferID + ");");
        Command1 decreSrcAccCmd = new UpdateBalanceCommand(srcAcc, -amount, DemoAtomicity.transferID);
        Command1 increDestAccCmd = new UpdateBalanceCommand(destAcc, amount, DemoAtomicity.transferID);

        decreSrcAccCmd.execute();
        // 后面的语句执行不到了
/*        increDestAccCmd.execute();

        DemoAtomicity.transferID++;*/
    }

}



class BankAccount {
    private final int id;
    private final String holderName;
    private float balance;

    public BankAccount(int id, String holderName, float balance) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance;
    }

    public int getId() {
        return this.id;
    }

    public String getHolderName() {
        return this.holderName;
    }

    public float getBalance() {
        return this.balance;
    }

    public void updateBalance(float diff) {
        this.balance += diff;
    }
}

interface Command1 {
    void execute();
    void undo();
}


class UpdateBalanceCommand implements Command1 {
    private int id;
    private final BankAccount acc;
    private final float amount; // diff
    private final int transferID;
    private static int update_log_id = 1;

    public UpdateBalanceCommand(BankAccount acc, float amount, int transferID) {
        this.acc = acc;
        this.amount = amount;
        this.transferID = transferID;
    }

    public UpdateBalanceCommand(int id, BankAccount acc, float amount, int transferID) {
        this(acc, amount, transferID);
        this.id = id;
    }

    @Override
    public void execute() {
        this.acc.updateBalance(this.amount);
        this.updateBalanceInDB();

    }

    @Override
    public void undo() {
        this.acc.updateBalance(-this.amount);
        this.updateBalanceInDB();
    }

    // change database data
    private void updateBalanceInDB() {
        try {
            DBConn dbConn = new DBConn("localhost:3306", "DemoTransaction", "myusername", "123456");
            dbConn.nonReadQuery("UPDATE BankAccounts SET balance = " + this.acc.getBalance() + " WHERE account_id = " + this.acc.getId() + ";");
            dbConn.nonReadQuery("INSERT INTO BalanceUpdateLog (`account_id`, `amount_change`) VALUES (" + this.acc.getId() + "," +this.amount + ");");
            if (this.amount < 0) { // srcAcc
                dbConn.nonReadQuery("UPDATE TransactionLog SET src_account_update_log_id = " + UpdateBalanceCommand.update_log_id++ + " WHERE tran_log_id = " + this.transferID + ";");
            } else { // destAcc
                dbConn.nonReadQuery("UPDATE TransactionLog SET dest_account_update_log_id = " + UpdateBalanceCommand.update_log_id++ + " WHERE tran_log_id = " + this.transferID + ";");
            }
        } catch (SQLException ex) {
            System.out.println("Exception during updating balance: " + ex);
        }

    }
}

// DAO：不带ORM的 Object Relational Mapping
class DBConn {
    private final Connection connection;
    private final Statement statement;
    private ResultSet resultSet;

    public DBConn(String host, String database, String username, String password) throws SQLException {
        DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);
        this.statement = this.connection.createStatement();
    }

    public void readQuery(String query) throws SQLException {
        this.resultSet = this.statement.executeQuery(query);

    }

    public void nonReadQuery(String query) throws SQLException{
        this.statement.execute(query);
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }


    @Override
    protected void finalize() throws SQLException{
        if (resultSet != null) {
            this.resultSet.close();
        }

        if (this.statement != null) {
            this.statement.close();
        }

        if (this.connection != null) {
            this.connection.close();
        }
    }
}


// ORM

class MyORM {
    private final ResultSet resultSet;
    public MyORM(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public Map<Integer, BankAccount> toBankAccount() throws SQLException {
        Map<Integer, BankAccount> rets = new HashMap<>();
        if (this.resultSet == null) {
            return rets;
        }
        while (this.resultSet.next()) {
            int account_id = (int) this.resultSet.getObject("account_id");
            String holder_name = (String) this.resultSet.getObject("holder_name");
            float balance = (float) this.resultSet.getObject("balance");
            rets.put(account_id, new BankAccount(account_id, holder_name, balance));
        }

        return rets;
    }

    public Set<UpdateBalanceCommand> toUpdateBalanceCommand(Map<Integer, BankAccount> bankAccounts, int trans_log_id) throws SQLException {
        Set<UpdateBalanceCommand> rets = new HashSet<>();
        if (this.resultSet == null) {
            return rets;
        }

        while (this.resultSet.next()) {
            int update_log_id = (int) this.resultSet.getObject("update_log_id");
            int account_id = (int) this.resultSet.getObject("account_id");
            float amount_change = (float) this.resultSet.getObject("amount_change");
            rets.add(new UpdateBalanceCommand(update_log_id, bankAccounts.get(account_id), amount_change, trans_log_id));
        }
        return rets;

    }
}
