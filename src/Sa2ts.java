import c3a.C3aInstParam;
import c3a.C3aOperand;
import sa.*;
import ts.Ts;
import ts.TsItemFct;
import ts.TsItemVar;

public class Sa2ts extends SaDepthFirstVisitor<Void> {
    private Ts tsGlobal = new Ts();
    private Ts currentLocalTable;

    private enum Context {GLOBAL, ARGUMENT, VARIABLE}

    ;
    private Context context;

    Sa2ts(SaNode root) {
        context = Context.GLOBAL;
        root.accept(this);
        context = Context.GLOBAL;

        if (FncDoesNotExists("main")) Throw.exception("main function must be declared ");
        if (tsGlobal.getFct("main").nbArgs > 0) Throw.exception("main function must not have arguments");
    }

    public Ts getTableGlobale() {
        return tsGlobal;
    }

    @Override
    public Void visit(SaDecTab node) {
        defaultIn(node);
        if (context != Context.GLOBAL) Throw.exception("Array should be declared as global");

        String nom = node.getNom();
        int taille = node.getTaille();

        if (varExists(nom, tsGlobal)) Throw.exception("This variable already exists");
        if (taille < 2) Throw.exception("Array size must be greater than or equal to 2");

        node.tsItem = tsGlobal.addVar(nom, taille);

        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaDecFonc node) {
        defaultIn(node);
        Ts localTable = new Ts();
        currentLocalTable = localTable;

        String nom = node.getNom();
        int nbArgs = node.getParametres() == null ? 0 : node.getParametres().length();

        context = Context.GLOBAL;

        if (FncExists(node.getNom())) Throw.exception("Fonction " + node.getNom() +
                " is already defined");

        context = Context.ARGUMENT;
        if (node.getParametres() != null) node.getParametres().accept(this);

        context = Context.VARIABLE;
        if (node.getVariable() != null) node.getVariable().accept(this);

        if (node.getCorps() != null) node.getCorps().accept(this);

        context = Context.GLOBAL;
        currentLocalTable = tsGlobal.getTableLocale(node.getNom());

        node.tsItem = tsGlobal.addFct(nom, nbArgs, localTable, node);
        defaultOut(node);
        return null;
    }


    @Override
    public Void visit(SaDecVar node) {
        defaultIn(node);
        switch (context) {
            case GLOBAL:
                declareVarGlobally(node);
                break;
            case ARGUMENT:
                declareParamLocally(node);
                break;
            case VARIABLE:
                declareVarLocally(node);
                break;
            default:
                Throw.exception("Unknown Context type");
        }
        defaultOut(node);
        return null;
    }


    @Override
    public Void visit(SaVarSimple node) {
        defaultIn(node);

        //This flag is independent of the context
        boolean isGlobal = false;
        if (varDoesNotExists(node.getNom(), currentLocalTable)) {
            if (varDoesNotExists(node.getNom(), tsGlobal)) {
                //Kids should not be allowed to see that
                throwVarNotDeclared(node.getNom());
            } else {
                isGlobal = true;
            }
        }

        TsItemVar tsItemVar;
        if (isGlobal) {
            tsItemVar = tsGlobal.getVar(node.getNom());
        } else {
            tsItemVar = currentLocalTable.getVar(node.getNom());
        }

        if (tsItemVar.taille > 1) {
            Throw.exception("Bad utilisation of array");
        }

        node.tsItem = tsItemVar;
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaAppel node) {
        defaultIn(node);
        String nom = node.getNom();

        int nbARgs = node.getArguments() == null ? 0 : node.getArguments().length();
        TsItemFct tsItemFct = tsGlobal.getFct(nom);

        if (FncDoesNotExists(nom)) Throw.exception("This function : " + nom + " does not exists");
        if (nbARgs != tsItemFct.nbArgs)
            Throw.exception("Expected " + tsItemFct.nbArgs + " argument(s) but got " + nbARgs);
        if(node.getArguments() != null){
            node.getArguments().getTete().accept(this);
            SaLExp queue = node.getArguments().getQueue();
            while(queue != null){
                queue.getTete().accept(this);
                queue = queue.getQueue();
            }
        }
        node.tsItem = tsItemFct;
        defaultOut(node);

        return null;
    }

    @Override
    public Void visit(SaVarIndicee node) {
        defaultIn(node);

        if (varExists(node.getNom(), currentLocalTable)) Throw.exception(node.getNom() +
                " is a local variable not a global array");
        if (varDoesNotExists(node.getNom(), tsGlobal)) throwVarNotDeclared(node.getNom());

        if (tsGlobal.getVar(node.getNom()).taille < 2) Throw.exception("Variable used as a variable");
        if(node.getIndice() != null ) node.getIndice().accept(this);
        node.tsItem = tsGlobal.getVar(node.getNom());

        defaultOut(node);
        return null;
    }

    @Override
    public void defaultIn(SaNode node) {
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }
        System.out.println("<" + node.getClass().getSimpleName() + ">");
        indentation++;
    }

    @Override
    public void defaultOut(SaNode node) {
        indentation--;
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }
        System.out.println("</" + node.getClass().getSimpleName() + ">");
    }

/***********************************************************************
 ***********************************************************************
 ***************************MÉTHODES PRIVÉES****************************
 ***********************************************************************
 ***********************************************************************/

    private boolean FncDoesNotExists(String nom) {
        return !FncExists(nom);
    }

    private int indentation = 0;


    private boolean varExists(String nom, Ts table) {
        return table.getVar(nom) != null;
    }

    private boolean varDoesNotExists(String nom, Ts table) {
        return !varExists(nom, table);
    }

    private boolean FncExists(String name) {
        return tsGlobal.getFct(name) != null;
    }

    private void throwVarNotDeclared(String varName) {
        Throw.exception("var : " + varName + " has not been declared");
    }

    private void throwVarDecTwice(String nom) {
        Throw.exception("var : " + nom + " is declared twice");
    }

    private void declareParamLocally(SaDecVar node) {
        if (varExists(node.getNom(), currentLocalTable)) throwVarDecTwice(node.getNom());
        node.tsItem = currentLocalTable.addParam(node.getNom());
    }

    private void declareVarLocally(SaDecVar node) {
        if (varExists(node.getNom(), currentLocalTable)) throwVarDecTwice(node.getNom());
        node.tsItem = currentLocalTable.addVar(node.getNom(), 1);
    }

    private void declareVarGlobally(SaDecVar node) {
        if (varExists(node.getNom(), tsGlobal)) throwVarDecTwice(node.getNom());
        node.tsItem = tsGlobal.addVar(node.getNom(), 1);
    }

    private static class Throw extends RuntimeException {
        private Throw(String message) {
            super(message);
        }

        public static void exception(String message) {
            throw new Throw(message);
        }
    }
}
