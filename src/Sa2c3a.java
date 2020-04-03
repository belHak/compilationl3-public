import c3a.*;
import sa.*;
import ts.Ts;
import ts.TsItemFct;
import ts.TsItemVar;

public class Sa2c3a extends SaDepthFirstVisitor<C3aOperand> {
    private C3a c3a;

    public Sa2c3a(SaNode root){
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

        C3aInstFBegin fBegin = new C3aInstFBegin(tsItemFct,"entree fonction");
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
        C3aTemp varTemp = c3a.newTemp();

        //dirty solution
        if(node.getArguments() != null){
            C3aOperand op =  node.getArguments().getTete().accept(this);
            c3a.ajouteInst(new C3aInstParam(op,""));
            SaLExp queue = node.getArguments().getQueue();
            while(queue != null){
                C3aOperand op2 = queue.getTete().accept(this);
                c3a.ajouteInst(new C3aInstParam(op2,""));
                queue = queue.getQueue();
            }
        }


        C3aInstCall call = new C3aInstCall(function,varTemp,"");
        c3a.ajouteInst(call);

        defaultOut(node);
        return varTemp;
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


        //This is tricky, but temps and labels have to be initialized before browsing another node
        //in order to guarantee priority
        C3aTemp temp = c3a.newTemp();

        C3aLabel vrai = c3a.newAutoLabel();
        C3aLabel faux = c3a.newAutoLabel();

        SaExp exp1 = node.getOp1();
        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        SaExp exp2 = node.getOp2();
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);

        //this order of declaration has to be respected
        //from here
        c3a.ajouteInst(new C3aInstJumpIfEqual(op1,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstJumpIfEqual(op2,c3a.False,faux,""));

        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));

        c3a.ajouteInst(new C3aInstJump(vrai,""));

        c3a.addLabelToNextInst(faux);

        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));

        c3a.addLabelToNextInst(vrai);
        //to here
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
        SaAppel val = node.getVal();

        C3aOperand operand = val == null ? null: val.accept(this);
        defaultOut(node);
        return operand ;
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

        C3aLabel startLabel = c3a.newAutoLabel();
        c3a.addLabelToNextInst(startLabel);
        C3aLabel checkLabel = c3a.newAutoLabel();

        C3aOperand op = node.getTest() == null? null :node.getTest().accept(this);

        c3a.ajouteInst(new C3aInstJumpIfEqual(op,c3a.False,checkLabel,""));
        if(node.getFaire() != null ) node.getFaire().accept(this);
        c3a.ajouteInst(new C3aInstJump(startLabel,""));
        c3a.addLabelToNextInst(checkLabel);
        return null;
    }

    @Override
    public C3aOperand visit(SaInstAffect node) {
        defaultIn(node);
        C3aOperand op1 = node.getLhs() == null ? null : node.getLhs().accept(this);
        C3aOperand res =  node.getRhs() == null ? null: node.getRhs().accept(this);
        C3aInstAffect aff = new C3aInstAffect(res,op1,"");
        c3a.ajouteInst(aff);
        defaultOut(node);
        return null;
    }

    @Override
    public C3aOperand visit(SaExpInf node) {
        defaultIn(node);

        //temps and labels have to be initialized here too, before browsing another node
        C3aTemp temp = c3a.newTemp();
        C3aLabel label = c3a.newAutoLabel();

        C3aOperand op1 = node.getOp1() == null ? null : node.getOp1().accept(this);
        C3aOperand op2 = node.getOp2() == null ? null : node.getOp2().accept(this);

        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));

        c3a.ajouteInst(new C3aInstJumpIfLess(op1,op2,label,""));

        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));

        c3a.addLabelToNextInst(label);
        defaultOut(node);
        return temp;

    }

    @Override
    public C3aOperand visit(SaExpEqual node) {
        defaultIn(node);

        C3aLabel label0 = c3a.newAutoLabel();
        C3aLabel label1 = c3a.newAutoLabel();
        C3aTemp subTemp = c3a.newTemp();
        C3aTemp resultTemp = c3a.newTemp();


        SaExp exp1 = node.getOp1();
        C3aOperand op1 = exp1 == null ? null:  exp1.accept(this);

        SaExp exp2 = node.getOp2();
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);

        c3a.ajouteInst(new C3aInstSub(op1,op2,subTemp,""));
        c3a.ajouteInst(new C3aInstJumpIfEqual(subTemp,new C3aConstant(0),label1,""));

        c3a.ajouteInst(new C3aInstAffect(c3a.False,resultTemp,""));

        c3a.ajouteInst(new C3aInstJump(label0,""));

        c3a.addLabelToNextInst(label1);

        c3a.ajouteInst(new C3aInstAffect(c3a.True,resultTemp,""));

        c3a.addLabelToNextInst(label0);

        defaultOut(node);
        return resultTemp;
    }

    @Override
    public C3aOperand visit(SaExpOr node) {
        defaultIn(node);

        C3aTemp temp = c3a.newTemp();

        C3aLabel vrai = c3a.newAutoLabel();
        C3aLabel faux = c3a.newAutoLabel();

        SaExp exp1 = node.getOp1();
        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);

        SaExp exp2 = node.getOp2();
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);

        //this order of declaration has to be respected
        //from here
        c3a.ajouteInst(new C3aInstJumpIfNotEqual(op1,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstJumpIfNotEqual(op2,c3a.False,faux,""));

        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));
        c3a.ajouteInst(new C3aInstJump(vrai,""));

        c3a.addLabelToNextInst(faux);

        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));

        c3a.addLabelToNextInst(vrai);
        //to here
        defaultOut(node);
        return temp;
    }

    @Override
    public C3aOperand visit(SaExpNot node) {
        defaultIn(node);
        C3aTemp temp = c3a.newTemp();
        C3aLabel label = c3a.newAutoLabel();

        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));
        C3aOperand operand =  node.getOp1() == null ? null : node.getOp1().accept(this);

        c3a.ajouteInst(new C3aInstJumpIfEqual(operand,c3a.False,label,""));
        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));

        c3a.addLabelToNextInst(label);
        defaultOut(node);
        return temp;
    }

    @Override
    public C3aOperand visit(SaExpLire node) {
        return super.visit(node);
    }

    @Override
    public C3aOperand visit(SaInstSi node) {
        defaultIn(node);

        C3aLabel label0 = c3a.newAutoLabel();

        C3aOperand op1 = node.getTest() == null ? null : node.getTest().accept(this);

        c3a.ajouteInst(new C3aInstJumpIfEqual(op1,c3a.False,label0,""));

        if(node.getAlors() != null) node.getAlors().accept(this);

        if(node.getSinon() != null) {
            C3aLabel label1 = c3a.newAutoLabel();

            c3a.ajouteInst(new C3aInstJump(label1,""));

            c3a.addLabelToNextInst(label0);

            node.getSinon().accept(this);
            c3a.addLabelToNextInst(label1);
        }else {
            c3a.addLabelToNextInst(label0);
        }


        defaultOut(node);
        return op1;
    }

    @Override
    public C3aOperand visit(SaInstRetour node) {
        defaultIn(node);

        C3aOperand operand = node.getVal() == null ? null : node.getVal().accept(this);
        c3a.ajouteInst(new C3aInstReturn(operand,""));

        defaultOut(node);
        return operand;
    }

    @Override
    public C3aOperand visit(SaVarSimple node) {
        defaultIn(node);
        TsItemVar var = node.tsItem;
        defaultOut(node);
        return new C3aVar(var,null);
    }

    @Override
    public C3aOperand visit(SaVarIndicee node) {
        defaultIn(node);
        TsItemVar var = node.tsItem;
        C3aOperand taille = node.getIndice() == null ? null :  node.getIndice().accept(this);
        defaultOut(node);

        return new C3aVar(var, taille);
    }

    public C3a getC3a() {
        return c3a;
    }

}
