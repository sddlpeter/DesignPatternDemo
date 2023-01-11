public class DemoStrategy {
    public static void main(String[] args) {
        int m1 = 3, n1 = 100;
        int m2 = 100, n2 = 3;
        StrategyClient sc1 = new StrategyClient(m1, n1);
        StrategyClient sc2 = new StrategyClient(m2, n2);

        System.out.println(sc1.solve());
        System.out.println(sc2.solve());
    }
}

class StrategyClient {
    private RollingArrayStrategy strategy;
    private int m, n;
    public StrategyClient(int m, int n) {
        this.m = m;
        this.n = n;
        this.pickStrategy();
    }

    private void pickStrategy() {
        if (this.n * 1.0 / this.m < 10.0) {
            this.setStrategy(new RowRollingDp());
        } else {
            this.setStrategy(new ColRollingDp());
        }
    }

    private void setStrategy(RollingArrayStrategy strategy) {
        this.strategy = strategy;
    }

    public int solve() {
        return this.strategy.uniquePath(this.m, this.n);
    }

}



interface RollingArrayStrategy {
    int uniquePath(int m, int n);
}

class RowRollingDp implements RollingArrayStrategy {
    @Override
    public int uniquePath(int m, int n) {
        System.out.println("calling RowRollingDp.uniquePath(" + m + ", " + n + ").");
        int R = 2;
        int[][] f = new int[R][n];

        f[0][0] = 1;
        for (int j = 0; j < n; j++) {
            f[0][j] = 1;
        }

        for (int i = 0; i < m; i++) {
            f[i % R][0] = 1;
        }

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                f[i % R][j] = f[i % R][j - 1] + f[(i - 1) % R][j];
            }
        }
        return f[(m - 1) % R][n - 1];

    }
}


class ColRollingDp implements RollingArrayStrategy {
    public int uniquePath(int m, int n) {
        System.out.println("calling ColRollingDp.uniquePath(" + m + ", " + n + ").");
        int R = 2;
        int[][] f = new int[m][R];

        f[0][0] = 1;
        for (int j = 1; j < n; j++) {
            f[0][j % R] = 1;
        }

        for (int i = 1; i < m; i++) {
            f[i][0] = 1;
        }

        for (int j = 1; j < n; j++) {
            for (int i = 1; i < m; i++) {
                f[i][j % R] = f[i - 1][j % R] + f[i][(j - 1) % R];
            }
        }

        return f[m - 1][(n - 1) % R];
    }
}
