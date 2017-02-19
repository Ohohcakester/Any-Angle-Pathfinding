
public class MainClass {
    public static void main(String[] args) {


    }

    /*
    public static double pathfind(int sx, int sy, int ex, int ey) {
        AnyaSearch anya;
        String mapfile;
        BitpackedGrid grid = new BitpackedGrid(filename);
        anya = new AnyaSearch(new AnyaExpansionPolicy(grid));

        AnyaNode start = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        AnyaNode target = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        anya.mb_start_ = start;
        anya.mb_target_ = target;
        anya.run();
        pathLength = anya.mb_cost_;

        start.root.setLocation(sx, sy);
        start.interval.init(sx, sx, sy);
        target.root.setLocation(ex, ey);
        target.interval.init(ex, ex, ey);
    }

    public static void run_anya(String scenarioFilePath)
    {
        System.gc();
        AnyaExperimentLoader exploader = new AnyaExperimentLoader();
        List<ExperimentInterface> experiments = null;
        AnyaSearch anya;

        try
        {
            experiments = exploader.loadExperiments(scenarioFilePath); 
            if(experiments.size() == 0)
            {
                System.err.println("No experiments to run; finishing.");
                return;
            }
            String mapfile = experiments.get(0).getMapFile();
            anya = new AnyaSearch(new AnyaExpansionPolicy(mapfile));
            anya.verbose = ScenarioRunner.verbose;
        }
        catch(Exception e)
        {
            e.printStackTrace(System.err);
            return;
        }
        
        System.out.println("exp" + ";"+
                "alg" + ";" +
                "wallt_micro"+";"+
                "runt_micro" + ";"+
                "expanded"+";" +
                "generated"+ ";"+
                "heapops" +";"+
                "start" +";"+
                "target"  +";"+
                "gridcost"+";"+
                "realcost"+";"+
                "map");

        
        MicroBenchmark exp_runner = new MicroBenchmark(anya);
        AnyaNode start = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        AnyaNode target = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        anya.mb_start_ = start;
        anya.mb_target_ = target;
        for (int i = 0; i < experiments.size(); i++)
        {
            ExperimentInterface exp = experiments.get(i);
            start.root.setLocation(exp.getStartX(), exp.getStartY());
            start.interval.init(exp.getStartX(), exp.getStartX(), exp.getStartY());
            target.root.setLocation(exp.getEndX(), exp.getEndY());
            target.interval.init(exp.getEndX(), exp.getEndX(), exp.getEndY());
                        
            long wallt_micro = exp_runner.benchmark(1);
            double cost = anya.mb_cost_;
            long duration = (long)(exp_runner.getAvgTime()+0.5);
            
            System.out.println(i + ";"+ 
                    "AnyaSearch" + ";" +
                    wallt_micro + ";"+
                    duration + ";"+
                    anya.expanded + ";"+
                    anya.generated + ";"+
                    anya.heap_ops + ";" +
                    "("+ exp.getStartX()+","+ exp.getStartY() + ")" +";"+
                    "(" + exp.getEndX() + "," + exp.getEndY() + ")" +";"+
                    exp.getUpperBound()  +";"+
                    cost+";"+
                    exp.getMapFile());

        }
    }
    */
}