import sa.*;
import ts.Ts;

public class Sa2ts extends SaDepthFirstVisitor<Void> {

    Sa2ts(SaNode root){
        root.accept(this);
    }

    Ts globalTable = new Ts();
    Ts currentTable;
    private int indetation = 0;
    @Override
    public void defaultIn(SaNode node) {
        for (int i = 0 ; i< indetation ; i++)
            System.out.print(" ");
        System.out.println("<"+node.getClass().getSimpleName()+">");
        indetation++;
    }

    @Override
    public void defaultOut(SaNode node) {
        indetation--;
        for (int i = 0 ; i< indetation ; i++)
            System.out.print(" ");
        System.out.println("</"+node.getClass().getSimpleName()+">");
    }

    @Override
    public Void visit(SaDecTab node) {
        defaultIn(node);
        if(globalTable.getVar(node.getNom()) != null) throw new RuntimeException("le tableau existe déjà");
        node.tsItem = globalTable.addVar(node.getNom(),node.getTaille());
        defaultOut(node);

        return null;

    }

    @Override
    public Void visit(SaDecFonc node) {
        defaultIn(node);
        if(globalTable.getFct(node.getNom()) != null ){

            if(globalTable.getFct(node.getNom()).getNbArgs() == node.getParametres().length())
                throw new RuntimeException("la fonction existe déja");
        }

        Ts localTable = new Ts();
        int nbParam = node.getParametres() == null ? 0 : node.getParametres().length();
        int nbVar = node.getVariable() == null ? 0 : node.getVariable().length();


        SaDec tete =nbParam == 0 ? null : node.getParametres().getTete();
        SaLDec queue= nbParam == 0 ? null : node.getParametres().getQueue();

        for(int index = 0 ; index < nbParam; index++){
            if(localTable.getVar(tete.getNom()) != null)throw new RuntimeException("variable existe");
            if(tete instanceof SaDecTab)throw new RuntimeException("pas de tableau frr");
            localTable.addParam(tete.getNom());
            tete = queue.getTete();
            if(queue.getQueue() == null) break;
            queue = queue.getQueue();

        }

        tete = nbVar ==0 ? null : node.getVariable().getTete();
        queue = nbVar==0 ? null : node.getVariable().getQueue();



        for(int index = 0 ; index < nbVar; index++){
            if(localTable.getVar(tete.getNom()) != null)throw new RuntimeException("variable existe");
            if(tete instanceof SaDecTab)throw new RuntimeException("pas de tableau frr");
            localTable.addVar(tete.getNom(),1);
            tete = queue.getTete();
            if(queue.getQueue() == null) break;
            queue = queue.getQueue();

        }
        node.tsItem = globalTable.addFct(node.getNom(),nbParam,localTable,node);
        currentTable = localTable;
        node.getCorps().accept(this);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaDecVar node) {
        defaultIn(node);
        if(globalTable.getVar(node.getNom()) != null) throw new RuntimeException("la variable existe déjà");

        node.tsItem = globalTable.addVar(node.getNom(),1);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaVarSimple node) {
        defaultIn(node);
        if(currentTable.getVar(node.getNom()) == null){
            if(globalTable.getVar(node.getNom()) == null) throw new RuntimeException("variabale non declarée");

            else{
                if(globalTable.getVar(node.getNom()).getTaille() != 1) throw new RuntimeException("ce n'est pas  une variable simple");

                else
                    node.tsItem = globalTable.getVar(node.getNom());
            }
        }
        else {
            if(currentTable.getVar(node.getNom()).getTaille() != 1) throw new RuntimeException("ce n'est pas  une variable simple");

            else
                node.tsItem = currentTable.getVar(node.getNom());
        }
        defaultOut(node);
        return null ;

    }

    @Override
    public Void visit(SaAppel node) {
        defaultIn(node);
        if(globalTable.getFct(node.getNom()) == null) throw new RuntimeException("votre fonction n'existe pas");

        if(globalTable.getFct(node.getNom()).nbArgs != node.getArguments().length()) throw new RuntimeException("nombre d'arguments incoherent");

        node.tsItem = globalTable.getFct(node.getNom());
        defaultOut(node);
        return null ;
    }

    @Override
    public Void visit(SaVarIndicee node) {
        defaultIn(node);
        if(currentTable.getVar(node.getNom()) != null) throw new RuntimeException("c pas  un tableau");

        if(globalTable.getVar(node.getNom()) == null) throw new RuntimeException("le tableau n'existe pas");

        else if(globalTable.getVar(node.getNom()).getTaille() == 1) throw new RuntimeException("ce n'est pas  un tableau");
        node.tsItem = globalTable.getVar(node.getNom());
        defaultOut(node);
        return null;


    }

    public Ts getTableGlobale() {
        return globalTable;
    }
}
