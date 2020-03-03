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

        defaultIn(node);
        return null;
    }

    @Override
    public C3aOperand visit(SaExpInt node) {
        defaultIn(node);
        defaultOut(node);
        return new C3aConstant(node.getVal());
    }

    @Override
    public C3aOperand visit(SaInstEcriture node) {
        C3aOperand operand = node.getArg().accept(this);
        c3a.ajouteInst(new C3aInstWrite(operand,""));
        return operand;
    }

    @Override
    public C3aOperand visit(SaExp node) {
        return null;
    }

    public C3a getC3a() {
        return c3a;
    }
}
