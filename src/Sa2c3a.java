import c3a.*;
import sa.*;
import ts.Ts;
import ts.TsItemFct;
import ts.TsItemVar;

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
        C3aTemp t = c3a.newTemp();
        C3aInstCall call = new C3aInstCall(function,t,"");
        node.getArguments().accept(this);
        c3a.ajouteInst(call);

        defaultOut(node);
        return t;
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
        SaAppel val = node.getVal();

        C3aOperand operand = val == null ? null: val.accept(this);
        defaultOut(node);
        return operand ;
    }

    @Override
    public C3aOperand visit(SaLExp node) {
        defaultIn(node);
        int length = node.length();
        SaExp tete = node.getTete();
        SaLExp queue = node.getQueue();
        C3aOperand operand = null;
        for(int i =0; i<length; i++){

            if(tete != null) {
                operand =  tete.accept(this);
                c3a.ajouteInst(new C3aInstParam(operand,""));
                tete = queue.getTete();
            }
            if(queue.getQueue() != null)
            queue = queue.getQueue();
        }

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
        C3aOperand op1 = node.getLhs().accept(this);
        C3aOperand res = node.getRhs().accept(this);
        C3aInstAffect aff = new C3aInstAffect(res,op1,"");
        c3a.ajouteInst(aff);
        return null;
    }

    @Override
    public C3aOperand visit(SaExpInf node) {
        C3aOperand op1 = node.getOp1().accept(this);
        C3aOperand op2 = node.getOp2().accept(this);
        C3aOperand temp = c3a.newTemp();
        C3aLabel label = c3a.newAutoLabel();
        C3aInstAffect aff = new C3aInstAffect(c3a.True,temp,"");
        C3aInstJumpIfLess jumpIfLess = new C3aInstJumpIfLess(op1,op2,label,"");
        C3aInstAffect aff2 = new C3aInstAffect(c3a.False,temp,"");
        c3a.ajouteInst(aff);
        c3a.ajouteInst(jumpIfLess);
        c3a.ajouteInst(aff2);
        c3a.addLabelToNextInst(label);
        return temp;

    }

    @Override
    public C3aOperand visit(SaExpEqual node) {
        C3aOperand op1 = node.getOp1().accept(this);
        C3aOperand op2 = node.getOp2().accept(this);
        C3aLabel label = c3a.newAutoLabel();
        C3aInstJumpIfEqual equal = new C3aInstJumpIfEqual(op1,op2,label,"");
        c3a.ajouteInst(equal);
        c3a.addLabelToNextInst(label);
        return label;
    }

    @Override
    public C3aOperand visit(SaExpOr node) {
        defaultIn(node);

        SaExp exp1 = node.getOp1();
        SaExp exp2 = node.getOp2();
        C3aOperand op1 = exp1 == null ? null : exp1.accept(this);
        C3aOperand op2 = exp2 == null ? null : exp2.accept(this);

        C3aLabel vrai = c3a.newAutoLabel();
        C3aLabel faux = c3a.newAutoLabel();

        C3aTemp temp = c3a.newTemp();

        c3a.ajouteInst(new C3aInstJumpIfNotEqual(op1,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstJumpIfNotEqual(op2,c3a.False,faux,""));
        c3a.ajouteInst(new C3aInstAffect(c3a.False,temp,""));
        c3a.ajouteInst(new C3aInstJump(vrai,""));
        c3a.addLabelToNextInst(faux);
        c3a.ajouteInst(new C3aInstAffect(c3a.True,temp,""));
        c3a.addLabelToNextInst(vrai);
        defaultOut(node);
        return temp;
    }

    @Override
    public C3aOperand visit(SaExpNot node) {
        C3aOperand op1 = node.getOp1().accept(this);
        C3aOperand op2 = node.getOp2().accept(this);
        C3aLabel label = c3a.newAutoLabel();
        C3aInstJumpIfNotEqual notEqual = new C3aInstJumpIfNotEqual(op1,op2,label,"");
        c3a.ajouteInst(notEqual);
        c3a.addLabelToNextInst(label);
        return label;
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
        defaultIn(node);
        C3aOperand test = node.getTest().accept(this);
        C3aLabel faux = c3a.newAutoLabel();
        C3aLabel sinon = c3a.newAutoLabel();
        C3aLabel etiquete = c3a.newAutoLabel();
        if(node.getSinon() != null){
            etiquete = faux;
        }else etiquete = sinon;

        c3a.ajouteInst(new C3aInstJumpIfEqual(test,c3a.False,etiquete,""));
        node.getAlors().accept(this);

        if(node.getSinon() != null){
            c3a.ajouteInst(new C3aInstJump(sinon,""));
            c3a.addLabelToNextInst(faux);
            node.getSinon().accept(this);
        }
        c3a.addLabelToNextInst(sinon);
        defaultOut(node);
        return null;


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
        TsItemVar var = node.tsItem;
        return new C3aVar(var,null);
    }

    @Override
    public C3aOperand visit(SaVarIndicee node) {
        return super.visit(node);
    }

    public C3a getC3a() {
        return c3a;
    }

}
