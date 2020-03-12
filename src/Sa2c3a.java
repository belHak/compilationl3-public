import c3a.*;
import sa.*;
import ts.Ts;

public class Sa2c3a extends SaDepthFirstVisitor<C3aOperand> {
    private C3a c3a;
    private Ts table ;



    public Sa2c3a(SaNode root, Ts table) {
        this.table = table;
        this.c3a = new C3a();
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
    public C3aOperand visit(SaExp node) {
        defaultIn(node);
        node.accept(this);
        defaultOut(node);
        return null;
    }



    @Override
    public C3aOperand visit(SaExpInt node) {
        defaultIn(node);

        C3aConstant constant = new C3aConstant(node.getVal());
        defaultOut(node);
        return constant;
    }



    @Override
    public C3aOperand visit(SaExpVar node) {
        defaultIn(node);
        C3aOperand var = node.getVar() == null ? null : node.getVar().accept(this);
        defaultOut(node);
        return var;
    }



    @Override
    public C3aOperand visit(SaInstEcriture node) {
        defaultIn(node);
        C3aOperand operand = node.getArg() == null ? null: node.getArg().accept(this);

        C3aInstWrite write = new C3aInstWrite(operand,"");
        c3a.ajouteInst(write);
        defaultOut(node);


        return operand ;
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
    public C3aOperand visit(SaDecFonc node) {
        defaultIn(node);

        C3aFunction fonction = new C3aFunction(node.tsItem);

        c3a.ajouteInst(new C3aInstFBegin(node.tsItem,"entree fonction"));



        if(node.getCorps() != null)
            node.getCorps().accept(this);


        c3a.ajouteInst(new C3aInstFEnd(""));
        defaultOut(node);
        return  fonction ;

    }

    @Override
    public C3aOperand visit(SaInstAffect node) {
        defaultIn(node);
        C3aOperand result =node.getLhs() == null ? null: node.getLhs().accept(this);
        C3aOperand op1 = node.getRhs() == null ? null: node.getRhs().accept(this);
        C3aInstAffect instAffect = new C3aInstAffect(op1,result,"");


        c3a.ajouteInst(instAffect);
        defaultOut(node);


        return result ;
    }



    @Override
    public C3aOperand visit(SaLDec node) {
        return super.visit(node);
    }



    @Override
    public C3aOperand visit(SaVarSimple node) {
        defaultIn(node);



        C3aVar var = new C3aVar(node.tsItem,null);

        defaultOut(node);

        return var ;
    }



    @Override
    public C3aOperand visit(SaAppel node) {
        defaultIn(node);

        C3aFunction function = new C3aFunction(node.tsItem);

        defaultOut(node);
        return function ;
    }



    @Override
    public C3aOperand visit(SaExpAppel node) {
        defaultIn(node);

        C3aOperand temp = c3a.newTemp();
        C3aFunction function = new C3aFunction(node.getVal().tsItem);

        C3aInstCall call  = new C3aInstCall(function,temp,"");
        c3a.ajouteInst(call);

        defaultOut(node);
        return temp ;
    }



    @Override
    public C3aOperand visit(SaExpAdd node) {

        defaultIn(node);
        C3aOperand op1= node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2= node.getOp2() == null ? null : node.getOp2().accept(this);
        C3aTemp temp = c3a.newTemp();
        c3a.ajouteInst(new C3aInstAdd(op1,op2,temp,""));

        defaultOut(node);

        return temp;
    }

    @Override
    public C3aOperand visit(SaExpSub node) {

        defaultIn(node);
        C3aOperand op1= node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2= node.getOp2() == null ? null : node.getOp2().accept(this);
        C3aTemp temp = c3a.newTemp();
        c3a.ajouteInst(new C3aInstSub(op1,op2,temp,""));

        defaultOut(node);

        return temp;

    }



    @Override
    public C3aOperand visit(SaExpMult node) {

        defaultIn(node);
        C3aOperand op1= node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2= node.getOp2() == null ? null : node.getOp2().accept(this);
        C3aTemp temp = c3a.newTemp();
        c3a.ajouteInst(new C3aInstMult(op1,op2,temp,""));

        defaultOut(node);

        return temp;
    }



    @Override
    public C3aOperand visit(SaExpDiv node) {

        defaultIn(node);
        C3aOperand op1= node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2= node.getOp2() == null ? null : node.getOp2().accept(this);
        C3aTemp temp = c3a.newTemp();
        c3a.ajouteInst(new C3aInstDiv(op1,op2,temp,""));

        defaultOut(node);

        return temp;
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
    public C3aOperand visit(SaExpAnd node) {

        defaultIn(node);
        C3aOperand op1 = node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2 = new C3aConstant(0);
        C3aLabel l0 = c3a.newAutoLabel();
        C3aLabel l1 = c3a.newAutoLabel();
        C3aInstJumpIfEqual if1 = new C3aInstJumpIfEqual(op1,op2,l1,"");
        c3a.ajouteInst(if1);

        C3aOperand op3 = node.getOp1() == null ? null : node.getOp2().accept(this);

        C3aInstJumpIfEqual if2 = new C3aInstJumpIfEqual(op3,op2,l1,"");
        c3a.ajouteInst(if2);

        C3aTemp t0 = c3a.newTemp();
        C3aInstAffect affect1 = new C3aInstAffect(new C3aConstant(1),t0,"");
        c3a.ajouteInst(affect1);

        C3aInstJump jumpTol0 = new C3aInstJump(l0,"");
        c3a.ajouteInst(jumpTol0);

        c3a.addLabelToNextInst(l1);

        c3a.ajouteInst(new C3aInstAffect(new C3aConstant(0),t0,""));

        c3a.addLabelToNextInst(l0);

        defaultOut(node);
        return t0 ;
    }

    @Override
    public C3aOperand visit(SaExpOr node) {

        defaultIn(node);
        C3aOperand op1 = node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2 = new C3aConstant(0);
        C3aLabel l0 = c3a.newAutoLabel();
        C3aLabel l1 = c3a.newAutoLabel();
        C3aInstJumpIfNotEqual if1 = new C3aInstJumpIfNotEqual(op1,op2,l1,"");
        c3a.ajouteInst(if1);

        C3aOperand op3 = node.getOp1() == null ? null : node.getOp2().accept(this);

        C3aInstJumpIfNotEqual if2 = new C3aInstJumpIfNotEqual(op3,op2,l1,"");
        c3a.ajouteInst(if2);

        C3aTemp t0 = c3a.newTemp();
        C3aInstAffect affect1 = new C3aInstAffect(new C3aConstant(0),t0,"");
        c3a.ajouteInst(affect1);

        C3aInstJump jumpTol0 = new C3aInstJump(l0,"");
        c3a.ajouteInst(jumpTol0);

        c3a.addLabelToNextInst(l1);

        c3a.ajouteInst(new C3aInstAffect(new C3aConstant(1),t0,""));

        c3a.addLabelToNextInst(l0);

        defaultOut(node);
        return t0 ;
    }

    @Override
    public C3aOperand visit(SaExpNot node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaExpLire node) {

        c3a.ajouteInst(new C3aInstRead(null,""));

        return null ;
    }

    @Override
    public C3aOperand visit(SaInstBloc node) {

        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstSi node) {

        C3aOperand op1 = node.getTest() == null ? null : node.getTest().accept(this);
        C3aLabel l0 = c3a.newAutoLabel();
        C3aInstJumpIfEqual si = new C3aInstJumpIfEqual(op1,new C3aConstant(0),l0,"");
        c3a.ajouteInst(si);
        node.getAlors().accept(this);
        if(node.getSinon() ==null) {

            c3a.addLabelToNextInst(l0);
        }
        else{
            C3aLabel l1 = c3a.newAutoLabel();
            c3a.ajouteInst(new C3aInstJump(l1,""));
            c3a.addLabelToNextInst(l0);
            node.getSinon().accept(this);
            c3a.addLabelToNextInst(l1);


        }


        return null ;
    }

    @Override
    public C3aOperand visit(SaInstRetour node) {
        defaultIn(node);

        C3aOperand op1 = node.getVal() == null ? null : node.getVal().accept(this);
        C3aInstReturn ret = new C3aInstReturn(op1,"");
        c3a.ajouteInst(ret);

        defaultOut(node);

        return op1 ;
    }

    @Override
    public C3aOperand visit(SaLExp node) {

        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaVarIndicee node) {
        defaultIn(node);

        C3aOperand index = node.getIndice() == null ? null: node.getIndice().accept(this);

        C3aVar var = new C3aVar(node.tsItem,index);

        defaultOut(node);

        return var ;
    }

    public C3a getC3a() {
        return c3a;
    }
}

