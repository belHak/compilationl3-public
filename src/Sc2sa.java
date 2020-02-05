import sa.*;
import sc.analysis.DepthFirstAdapter;
import sc.node.*;

public class Sc2sa extends DepthFirstAdapter {

    private SaNode returnValue;

    @Override
    public void defaultIn(Node node) {
        System.out.println(node.getClass().getSimpleName());
//        System.out.println(node);
//        if(!(node instanceof ADecvarentierDecvar)){
//
//            System.out.println(node.getClass().getSimpleName());
//            System.out.println(node);
//        }

//        if(!(node instanceof Start)){
//            System.out.println("\t est le fils de ->"+node.getClass().getSimpleName());
//        }
    }

    @Override
    public void caseStart(Start node) {
        inStart(node);

        SaProg prog ;
        node.getPProgramme().apply(this);
        prog = (SaProg) this.returnValue;
        this.returnValue = prog;

    }

    @Override
    public void caseADecvarldecfoncProgramme(ADecvarldecfoncProgramme node) {
        inADecvarldecfoncProgramme(node);

        SaLDec vars;
        SaLDec foncs;
        node.getOptdecvar().apply(this);
        vars = (SaLDec) returnValue;
        node.getListedecfonc().apply(this);
        foncs = (SaLDec)  returnValue;

        returnValue = new SaProg(vars,foncs);
    }

    @Override
    public void caseALdecfoncProgramme(ALdecfoncProgramme node) {
        inALdecfoncProgramme(node);
        SaLDec foncs;
        node.getListedecfonc().apply(this);
        foncs = (SaLDec)  returnValue;

        returnValue = new SaProg(null,foncs);
    }

    @Override
    public void caseAOptdecvar(AOptdecvar node) {
        inAOptdecvar(node);
        SaLDec vars;
        node.getListedecvar().apply(this);

        vars = (SaLDec) returnValue;
        returnValue = vars;
    }

    @Override
    public void caseADecvarldecvarListedecvar(ADecvarldecvarListedecvar node) {
        inADecvarldecvarListedecvar(node);
        SaDec tete;
        SaLDec queue;

        node.getDecvar().apply(this);
        tete = (SaDec) returnValue;
        node.getListedecvarbis().apply(this);
        queue = (SaLDec) returnValue;
        returnValue = new SaLDec(tete,queue);
    }

    @Override
    public void caseADecvarListedecvar(ADecvarListedecvar node) {
        inADecvarListedecvar(node);
        SaDec var = null;
        node.getDecvar().apply(this);
        var = (SaDec) this.returnValue;
        this.returnValue = new SaLDec(var,null);

    }

    @Override
    public void caseADecvarldecvarListedecvarbis(ADecvarldecvarListedecvarbis node) {
        inADecvarldecvarListedecvarbis(node);
        SaDec tete;
        SaLDec queue;

        node.getDecvar().apply(this);
        tete = (SaDec) returnValue;
        node.getListedecvarbis().apply(this);
        queue = (SaLDec) returnValue;
        System.out.println(queue.toString());

        returnValue = new SaLDec(tete,queue);
    }

    @Override
    public void caseADecvarListedecvarbis(ADecvarListedecvarbis node) {
        inADecvarListedecvarbis(node);
        SaDec tete;

        node.getDecvar().apply(this);

        tete = (SaDec) returnValue;

        returnValue = new SaLDec(tete,null);
    }

    @Override
    public void caseADecvarentierDecvar(ADecvarentierDecvar node) {
        inADecvarentierDecvar(node);
        String nom;

        nom = node.getIdentif().getText();
        returnValue = new SaDecVar(nom);
    }

    @Override
    public void caseADecvartableauDecvar(ADecvartableauDecvar node) {
        inADecvartableauDecvar(node);
        String nom;
        int taille;

        nom = node.getIdentif().getText();
        taille = Integer.parseInt(node.getNombre().getText());

        returnValue = new SaDecTab(nom,taille);
    }

    @Override
    public void caseALdecfoncrecListedecfonc(ALdecfoncrecListedecfonc node) {
        inALdecfoncrecListedecfonc(node);
        SaDecFonc fonc;
        SaLDec foncs;
        node.getDecfonc().apply(this);
        fonc = (SaDecFonc) returnValue;
        node.getListedecfonc().apply(this);
        foncs = (SaLDec) returnValue;

        returnValue = new SaLDec(fonc,foncs);

    }

    @Override
    public void caseALdecfoncfinalListedecfonc(ALdecfoncfinalListedecfonc node) {
        inALdecfoncfinalListedecfonc(node);

       this.returnValue = null;
    }

    @Override
    public void caseADecvarinstrDecfonc(ADecvarinstrDecfonc node) {
        inADecvarinstrDecfonc(node);
        //DANGER
        String nom;
        SaLDec parms;
        SaLDec decvar;
        SaInstBloc bloc;

        nom = node.getIdentif().getText();
        node.getListeparam().apply(this);
        parms = (SaLDec) this.returnValue;

        node.getOptdecvar().apply(this);
        decvar = (SaLDec) this.returnValue;
        node.getInstrbloc().apply(this);
        bloc = (SaInstBloc) this.returnValue;

        this.returnValue = new SaDecFonc(nom,parms,decvar,bloc);
    }

    @Override
    public void caseAInstrDecfonc(AInstrDecfonc node) {
        inAInstrDecfonc(node);
        String nom;
        SaLDec parms;
        SaInstBloc bloc;

        nom = node.getIdentif().getText();
        node.getListeparam().apply(this);
        parms = (SaLDec) this.returnValue;

        node.getInstrbloc().apply(this);
        bloc = (SaInstBloc) this.returnValue;

        this.returnValue = new SaDecFonc(nom,parms,null,bloc);
    }

    @Override
    public void caseASansparamListeparam(ASansparamListeparam node) {
        inASansparamListeparam(node);
        this.returnValue = /*new SaLDec(null,null)*/ null;
    }

    @Override
    public void caseAAvecparamListeparam(AAvecparamListeparam node) {
        inAAvecparamListeparam(node);
        SaLDec vars ;
        node.getListedecvar().apply(this);
        vars =(SaLDec) returnValue;
        returnValue = vars;

    }

    @Override
    public void caseAInstraffectInstr(AInstraffectInstr node) {
        inAInstraffectInstr(node);
        SaInstAffect affect ;
        node.getInstraffect().apply(this);
        affect = (SaInstAffect) returnValue;
        returnValue = affect;
    }

    @Override
    public void caseAInstrblocInstr(AInstrblocInstr node) {
        inAInstrblocInstr(node);
        SaInstBloc bloc ;
        node.getInstrbloc().apply(this);
        bloc = (SaInstBloc) returnValue;
        returnValue = bloc;
    }

    @Override
    public void caseAInstrsiInstr(AInstrsiInstr node) {
        inAInstrsiInstr(node);

        SaInstSi si ;
        node.getInstrsi().apply(this);
        si = (SaInstSi) returnValue;
        returnValue = si;
    }

    @Override
    public void caseAInstrtantqueInstr(AInstrtantqueInstr node) {
        inAInstrtantqueInstr(node);

        SaInstTantQue tq ;
        node.getInstrtantque().apply(this);
        tq = (SaInstTantQue) returnValue;
        returnValue = tq;
    }

    @Override
    public void caseAInstrappelInstr(AInstrappelInstr node) {
        inAInstrappelInstr(node);

        SaAppel appel ;
        node.getInstrappel().apply(this);
        appel = (SaAppel) returnValue;
        returnValue = appel;
    }

    @Override
    public void caseAInstrretourInstr(AInstrretourInstr node) {
        inAInstrretourInstr(node);

        SaInstRetour retour ;
        node.getInstrretour().apply(this);
        retour = (SaInstRetour) returnValue;
        returnValue = retour;
    }

    @Override
    public void caseAInstrecritureInstr(AInstrecritureInstr node) {
        inAInstrecritureInstr(node);

        SaInstEcriture ecriture ;
        node.getInstrecriture().apply(this);
        ecriture = (SaInstEcriture) returnValue;
        returnValue = ecriture;
    }

    @Override
    public void caseAInstrvideInstr(AInstrvideInstr node) {
        inAInstrvideInstr(node);
        SaInst vide ;
        node.getInstrvide().apply(this);
        vide = (SaInst) returnValue;
        returnValue = vide;
    }

    @Override
    public void caseAInstraffect(AInstraffect node) {
        inAInstraffect(node);

        SaVar op1;
        SaExp op2;
        node.getVar().apply(this);
        op1 = (SaVar) returnValue;
        node.getExp().apply(this);
        op2 = (SaExp) returnValue;

        returnValue = new SaInstAffect(op1, op2);
    }

    @Override
    public void caseAInstrbloc(AInstrbloc node) {
        inAInstrbloc(node);
        SaLInst inst;

        node.getListeinst().apply(this);
        inst = (SaLInst) returnValue;
        returnValue = new SaInstBloc(inst);
    }

    @Override
    public void caseALinstrecListeinst(ALinstrecListeinst node) {
        inALinstrecListeinst(node);

        SaInst tete;
        SaLInst queue;

        node.getInstr().apply(this);
        tete = (SaInst) returnValue;
        node.getListeinst().apply(this);
        queue = (SaLInst) returnValue;
        returnValue = new SaLInst(tete,queue);
    }

    @Override
    public void caseALinstfinalListeinst(ALinstfinalListeinst node) {
        inALinstfinalListeinst(node);
        returnValue =  null;
    }

    @Override
    public void caseAAvecsinonInstrsi(AAvecsinonInstrsi node) {
        inAAvecsinonInstrsi(node);
        SaExp test;
        SaInst alors;
        SaInst sinon;

        node.getExp().apply(this);
        test = (SaExp) returnValue;
        node.getInstrbloc().apply(this);
        alors = (SaInst) returnValue;
        node.getInstrsinon().apply(this);
        sinon = (SaInst) returnValue;

        returnValue = new SaInstSi(test,alors,sinon);
    }

    @Override
    public void caseASanssinonInstrsi(ASanssinonInstrsi node) {
        inASanssinonInstrsi(node);

        SaExp test;
        SaInst alors;

        node.getExp().apply(this);
        test = (SaExp) returnValue;
        node.getInstrbloc().apply(this);alors = (SaInst) returnValue;

        returnValue = new SaInstSi(test,alors,null);
    }

    @Override
    public void caseAInstrsinon(AInstrsinon node) {
        inAInstrsinon(node);
        SaInstBloc bloc ;
        node.getInstrbloc().apply(this);
        bloc = (SaInstBloc) returnValue;
        returnValue = bloc;
    }

    @Override
    public void caseAInstrtantque(AInstrtantque node) {
        inAInstrtantque(node);


        SaExp test;
        SaInst faire;

        node.getExp().apply(this);
        test = (SaExp) returnValue;
        node.getInstrbloc().apply(this);
        faire = (SaInst) returnValue;
        returnValue = new SaInstTantQue(test, faire);
    }

    @Override
    public void caseAInstrappel(AInstrappel node) {
        inAInstrappel(node);
        SaAppel appel;

        node.getAppelfct().apply(this);
        appel = (SaAppel) returnValue;
        returnValue = appel;
    }

    @Override
    public void caseAInstrretour(AInstrretour node) {
        inAInstrretour(node);

        SaExp val;
        node.getExp().apply(this);
        val = (SaExp) returnValue;
        returnValue = new SaInstRetour(val);
    }

    @Override
    public void caseAInstrecriture(AInstrecriture node) {
       inAInstrecriture(node);
       SaExp exp;
       node.getExp().apply(this);
       exp = (SaExp) returnValue;
       returnValue = new SaInstEcriture(exp);
    }

    @Override
    public void caseAInstrvide(AInstrvide node) {
        inAInstrvide(node);
        this.returnValue = null;
    }

    @Override
    public void caseAOuExp(AOuExp node) {
        inAOuExp(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp1().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpOr(ex1,ex2);
    }

    @Override
    public void caseAExp1Exp(AExp1Exp node) {
        inAExp1Exp(node);

        SaExp ex1;
        node.getExp1().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseAEtExp1(AEtExp1 node) {
        inAEtExp1(node);

        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp1().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp2().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpAnd(ex1,ex2);
    }

    @Override
    public void caseAExp2Exp1(AExp2Exp1 node) {
        inAExp2Exp1(node);
        SaExp ex1;
        node.getExp2().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseAInfExp2(AInfExp2 node) {
        inAInfExp2(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp2().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp3().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpInf(ex1,ex2);
    }

    @Override
    public void caseAEgalExp2(AEgalExp2 node) {
        inAEgalExp2(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp2().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp3().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpEqual(ex1,ex2);

    }

    @Override
    public void caseAExp3Exp2(AExp3Exp2 node) {
        inAExp3Exp2(node);
        SaExp ex1;
        node.getExp3().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseAPlusExp3(APlusExp3 node) {
        inAPlusExp3(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp3().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp4().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpAdd(ex1,ex2);
    }

    @Override
    public void caseAMoinsExp3(AMoinsExp3 node) {
        inAMoinsExp3(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp3().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp4().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpSub(ex1,ex2);
    }

    @Override
    public void caseAExp4Exp3(AExp4Exp3 node) {
        inAExp4Exp3(node);
        SaExp ex1;
        node.getExp4().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseAFoisExp4(AFoisExp4 node) {
        inAFoisExp4(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp4().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp5().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpMult(ex1,ex2);
    }

    @Override
    public void caseADiviseExp4(ADiviseExp4 node) {
        inADiviseExp4(node);
        SaExp ex1 ;
        SaExp ex2 ;

        node.getExp4().apply(this);
        ex1 = (SaExp) returnValue;
        node.getExp5().apply(this);
        ex2 = (SaExp) returnValue;
        returnValue = new SaExpDiv(ex1,ex2);
    }

    @Override
    public void caseAExp5Exp4(AExp5Exp4 node) {
        inAExp5Exp4(node);
        SaExp ex1;
        node.getExp5().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseANonExp5(ANonExp5 node) {
        inANonExp5(node);
        SaExp ex1;
        node.getExp5().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = new SaExpNot(ex1);
    }

    @Override
    public void caseAExp6Exp5(AExp6Exp5 node) {
        inAExp6Exp5(node);
        SaExp ex1;
        node.getExp6().apply(this);
        ex1 = (SaExp) returnValue;
        returnValue = ex1;
    }

    @Override
    public void caseANombreExp6(ANombreExp6 node) {
        inANombreExp6(node);
        int ex1;
        ex1 = Integer.parseInt(node.getNombre().getText());
        returnValue = new SaExpInt(ex1);
    }

    @Override
    public void caseAAppelfctExp6(AAppelfctExp6 node) {
        inAAppelfctExp6(node);
        SaAppel appel;
        node.getAppelfct().apply(this);
        appel = (SaAppel) returnValue;
        returnValue = appel;
    }

    @Override
    public void caseAVarExp6(AVarExp6 node) {
        inAVarExp6(node);
        SaVar var;
        node.getVar().apply(this);
        var = (SaVar) returnValue;
        returnValue =  new SaExpVar(var);
    }

    @Override
    public void caseAParenthesesExp6(AParenthesesExp6 node) {
        inAParenthesesExp6(node);

        SaExp exp ;

        node.getExp().apply(this);
        exp = (SaExp) returnValue;
        returnValue = exp;
    }

    @Override
    public void caseALireExp6(ALireExp6 node) {
        inALireExp6(node);
        returnValue = new SaExpLire();
    }

    @Override
    public void caseAVartabVar(AVartabVar node) {
        inAVartabVar(node);

        String nom;
        SaExp indice;

        nom = node.getIdentif().getText();
        node.getExp().apply(this);
        indice = (SaExp) returnValue;
        returnValue = new SaVarIndicee(nom,indice);
    }

    @Override
    public void caseAVarsimpleVar(AVarsimpleVar node) {
        inAVarsimpleVar(node);
        String nom;

        nom = node.getIdentif().getText();
        returnValue = new SaVarSimple(nom);
    }

    @Override
    public void caseARecursifListeexp(ARecursifListeexp node) {
        inARecursifListeexp(node);
        SaExp tete;
        SaLExp queue;

        node.getExp().apply(this);
        tete = (SaExp) returnValue;
        node.getListeexpbis().apply(this);
        queue = (SaLExp) returnValue;
        returnValue = new SaLExp(tete,queue);
    }

    @Override
    public void caseAFinalListeexp(AFinalListeexp node) {
        this.returnValue = null;
    }

    @Override
    public void caseARecursifListeexpbis(ARecursifListeexpbis node) {
        inARecursifListeexpbis(node);
        SaExp tete;
        SaLExp queue;

        node.getExp().apply(this);
        tete = (SaExp) returnValue;
        node.getListeexpbis().apply(this);
        queue = (SaLExp) returnValue;
        returnValue = new SaLExp(tete,queue);
    }

    @Override
    public void caseAFinalListeexpbis(AFinalListeexpbis node) {
        inAFinalListeexpbis(node);
        returnValue = null;
    }

    @Override
    public void caseAAppelfct(AAppelfct node) {
        inAAppelfct(node);

        String nom;
        SaLExp arg;

        nom = node.getIdentif().getText();
        node.getListeexp().apply(this);
        arg = (SaLExp) returnValue;
        returnValue = new SaAppel(nom,arg);
    }

    public SaNode getRoot(){
        return this.returnValue;
    }
}
