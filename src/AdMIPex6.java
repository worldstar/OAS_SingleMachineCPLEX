import ilog.concert.*;
import ilog.cplex.*;


public class AdMIPex6 {
   static class Solve extends IloCplex.SolveCallback {
      boolean     _done = false;
      IloNumVar[] _vars;
      double[]    _x;
      Solve(IloNumVar[] vars, double[] x) { _vars = vars; _x = x; }
    
      public void main() throws IloException {
         if ( !_done ) {
            setStart(_x, _vars, null, null);
            _done = true;
         }
      }
   }

   public static void main(String[] args) {
      try {
         IloCplex cplex = new IloCplex();
       
         cplex.importModel(args[0]);
         IloLPMatrix lp = (IloLPMatrix)cplex.LPMatrixIterator().next();
       
         IloConversion relax = cplex.conversion(lp.getNumVars(),
                                                IloNumVarType.Float);
         cplex.add(relax);

         cplex.solve();
         System.out.println("Relaxed solution status = " + cplex.getStatus());
         System.out.println("Relaxed solution value  = " + cplex.getObjValue());

         double[] vals = cplex.getValues(lp.getNumVars());
         cplex.use(new Solve(lp.getNumVars(), vals));

         cplex.delete(relax);
       
         cplex.setParam(IloCplex.Param.MIP.Strategy.Search, IloCplex.MIPSearch.Traditional);
         if ( cplex.solve() ) {
            System.out.println("Solution status = " + cplex.getStatus());
            System.out.println("Solution value  = " + cplex.getObjValue());
         }
         cplex.end();
      }
      catch (IloException e) {
         System.err.println("Concert exception caught: " + e);
      }
   }
}