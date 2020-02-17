import sa.*;
import ts.Ts;
import ts.TsItemFct;
import ts.TsItemVar;

public class Sa2ts extends SaDepthFirstVisitor<Void> {
    private Ts tsGlobal = new Ts();
    private Ts currentLocalTable;

    Sa2ts(SaNode root) {
        root.accept(this);
        if(FncDoesNotExists("main")) throw new RuntimeException("main function must be declared ");
        if(tsGlobal.getFct("main").nbArgs > 0){
            throw new RuntimeException("main function must not have arguments");
        }
    }

    public Ts getTableGlobale() {
        return tsGlobal;
    }

    @Override
    public Void visit(SaDecTab node) {
        defaultIn(node);
        String nom = node.getNom();
        //Should throw Exception;
        if (varExists(nom, tsGlobal)) return null;

        int taille = node.getTaille();
        if (taille < 2) throw new IllegalArgumentException("array size must be greater than or equal to 2");
        node.tsItem = tsGlobal.addVar(nom, taille);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaDecFonc node) {
        defaultIn(node);

        //Throw RuntimeException to avoid declaring it explicitly in the method
        if (FncExists(node.getNom())) throw new RuntimeException("Fonction " + node.getNom() +
                " is already defined");

        //Returns boolean but should return void
        if (!addFunctionToSymbolTable(node)) return null;

        currentLocalTable = tsGlobal.getTableLocale(node.getNom());
        node.getCorps().accept(this);

        defaultOut(node);
        return null;
    }


    @Override
    public Void visit(SaDecVar node) {
        defaultIn(node);
        if (varExists(node.getNom(), tsGlobal)) {
            throwVarDecTwice(node.getNom());
        }
        node.tsItem = tsGlobal.addVar(node.getNom(), 1);
        defaultOut(node);
        return null;
    }


    @Override
    public Void visit(SaVarSimple node) {
        defaultIn(node);
        //doit vérifier si la variable existe dans cette portée

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
            throw new RuntimeException("Bad utilisation of array");
        }

        node.tsItem = tsItemVar;
        defaultOut(node);
        return null;
    }


    @Override
    public Void visit(SaAppel node) {
        defaultIn(node);
        String nom = node.getNom();

        int nbARgs = node.getArguments().length();
        TsItemFct tsItemFct = tsGlobal.getFct(nom);

        if(FncDoesNotExists(nom)) throw new RuntimeException("This function : "+nom+" does not exists");
        if(nbARgs != tsItemFct.nbArgs) throw new RuntimeException("Expected "+tsItemFct.nbArgs+" argument(s) but got "+nbARgs);

        node.tsItem = tsItemFct;
        defaultOut(node);

        return null;
    }

    private boolean FncDoesNotExists(String nom) {
        return !FncExists(nom);
    }

    @Override
    public Void visit(SaVarIndicee node) {
        defaultIn(node);

        if (varExists(node.getNom(), currentLocalTable))
            throw new RuntimeException(node.getNom() + " is a local variable not a global array");
        if (varDoesNotExists(node.getNom(), tsGlobal))
             throwVarNotDeclared(node.getNom());

        if (tsGlobal.getVar(node.getNom()).taille < 2) {
            throw new RuntimeException("this is not an array");
        }
        node.tsItem = tsGlobal.getVar(node.getNom());

        defaultOut(node);
        return null;
    }

    private int indentation = 0;

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

    private boolean addFunctionToSymbolTable(SaDecFonc node) {
        Ts localTable = new Ts();
        String nom = node.getNom();

        int nbArgs = node.getParametres() == null ? 0 : node.getParametres().length();
        int nbVar = node.getVariable() == null ? 0 : node.getVariable().length();

        SaLDec varIterateur = node.getVariable();
        SaDec teteVarList = nbVar == 0 ? null : varIterateur.getTete();
        for (int i = 0; i < nbVar; i++) {


            if (varExists(teteVarList.getNom(), localTable))
                throwVarDecTwice(teteVarList.getNom());
            if (teteVarList instanceof SaDecTab)
                throwNotGlobalArray();

            localTable.addVar(teteVarList.getNom(), 1);

            if (varIterateur.getQueue() == null) break;
            varIterateur = varIterateur.getQueue();
            teteVarList = varIterateur.getTete();
        }

        SaLDec argIterateur = node.getParametres();
        SaDec teteParamList = nbArgs == 0 ? null : argIterateur.getTete();
        for (int i = 0; i < nbArgs; i++) {

            if (varExists(teteParamList.getNom(), localTable))
                throwVarDecTwice(teteParamList.getNom());
            if (teteParamList instanceof SaDecTab) {
                 throwNotGlobalArray();
            }

            localTable.addParam(teteParamList.getNom());

            if (argIterateur.getQueue() == null) break;
            argIterateur = argIterateur.getQueue();
            teteParamList = argIterateur.getTete();
        }

        node.tsItem = tsGlobal.addFct(nom, nbArgs, localTable, node);
        return true;
    }

    private void throwNotGlobalArray() {
        throw new IllegalArgumentException("Arrays should be declared as global variables");
    }


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
        throw new RuntimeException("var : " + varName + " has not been declared");
    }

    private void throwVarDecTwice(String nom) {
        throw new IllegalArgumentException("var : " + nom + " is declared twice");
    }
}
