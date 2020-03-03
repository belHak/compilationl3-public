import c3a.*;
import sa.*;
import ts.Ts;
import ts.TsItemFct;

public class Sa2c3a extends SaDepthFirstVisitor<C3aOperand> {
    private C3a c3a;
    private Ts table;

    public Sa2c3a(SaNode root, Ts table){
        this.table = table;
        c3a = new C3a();
        root.accept(this);
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

    @Override
    public C3aOperand visit(SaDecFonc node) {
        defaultIn(node);
        TsItemFct tsItemFct = node.tsItem;

        C3aInstFBegin fBegin = new C3aInstFBegin(tsItemFct,"");
        c3a.ajouteInst(fBegin);

        SaInst inst = tsItemFct.saDecFonc.getCorps();
        inst.accept(this);

        C3aInstFEnd fEnd = new C3aInstFEnd("Fin de la fonction "+tsItemFct.identif);
        c3a.ajouteInst(fEnd);


        defaultOut(node);
        return new C3aFunction(tsItemFct);
    }

    @Override
    public C3aOperand visit(SaAppel node) {
        defaultIn(node);

        TsItemFct tsItemFct = node.tsItem;
        C3aFunction function = new C3aFunction(tsItemFct);

        C3aInstCall call = new C3aInstCall(function,c3a.newTemp(),"");

        c3a.ajouteInst(call);

        defaultOut(node);
        return function;
    }

    @Override
    public C3aOperand visit(SaExpAdd node) {
        defaultIn(node);
        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();

        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);
        C3aTemp result = c3a.newTemp();

        c3a.ajouteInst(new C3aInstAdd(op1,op2, result,""));

        defaultOut(node);

        return result;
    }

    @Override
    public C3aOperand visit(SaExpSub node) {
        defaultIn(node);
        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();

        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);
        C3aTemp result = c3a.newTemp();

        c3a.ajouteInst(new C3aInstSub(op1,op2, result,""));

        defaultOut(node);

        return result;
    }

    @Override
    public C3aOperand visit(SaExpDiv node) {
        defaultIn(node);
        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();

        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);
        C3aTemp result = c3a.newTemp();

        c3a.ajouteInst(new C3aInstDiv(op1,op2, result,""));

        defaultOut(node);

        return result;
    }

    @Override
    public C3aOperand visit(SaExpMult node) {
        defaultIn(node);
        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();

        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);
        C3aTemp result = c3a.newTemp();

        c3a.ajouteInst(new C3aInstMult(op1,op2, result,""));

        defaultOut(node);

        return result;
    }

    @Override
    public C3aOperand visit(SaExpAnd node) {
        defaultIn(node);

        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();
        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);

        C3aLabel vrai = c3a.newAutoLabel();
        C3aLabel faux = c3a.newAutoLabel();

        C3aTemp temp = c3a.newTemp();

        c3a.ajouteInst(new C3aInstJumpIfEqual(op1,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstJumpIfEqual(op2,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));
        c3a.ajouteInst(new C3aInstJump(vrai,""));
        c3a.addLabelToNextInst(faux);
        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));
        c3a.addLabelToNextInst(vrai);
        defaultOut(node);
        return temp;
    }

    @Override
    public C3aOperand visit(SaExpInt node) {
        defaultIn(node);
        defaultOut(node);
        return new C3aConstant(node.getVal());
    }

    @Override
    public C3aOperand visit(SaInstEcriture node) {
        defaultIn(node);

        SaExp exp = node.getArg();
        C3aOperand operand = exp == null ? null : exp.accept(this) ;
        c3a.ajouteInst(new C3aInstWrite(operand,""));

        defaultOut(node);
        return operand;
    }

    @Override
    public C3aOperand visit(SaExpAppel node) {
        defaultIn(node);
        SaLExp arg = node.getVal().getArguments();
        if(arg != null) arg.accept(this);

        defaultOut(node);
        return  null;
    }

    @Override
    public C3aOperand visit(SaLExp node) {
        defaultIn(node);

        SaExp exp = node.getTete();
        C3aOperand operand = exp == null ? null : exp.accept(this);
        new C3aInstParam(operand,"");

        defaultOut(node);
        return operand;
    }


    @Override
    public C3aOperand visit(SaExpVar node) {
        defaultIn(node);
        SaVar var = node.getVar();
        C3aOperand operande = var == null ? null :  var.accept(this);
        defaultOut(node);
        return operande;
    }

    @Override
    public C3aOperand visit(SaInstTantQue node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaLInst node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstAffect node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpInf node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpEqual node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpOr node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpNot node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpLire node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstBloc node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstSi node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstRetour node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaVarSimple node) {
        // doit renvoyer une instance de C3aVar
        //return new C3aVar(node.tsItem,)
        return null;
    }

    @Override
    public C3aOperand visit(SaVarIndicee node) {
        return super.visit(node);
    }

    public C3a getC3a() {
        return c3a;
    }
}
