import c3a.*;
import nasm.*;
import ts.Ts;
import ts.TsItemVar;

@SuppressWarnings("FieldCanBeLocal")
public class C3a2nasm implements C3aVisitor<NasmOperand> {
    private Ts table;
    private Ts local;
    private C3a c3a;
    private Nasm nasm;

    C3a2nasm(C3a c3a, Ts table) {
        nasm = new Nasm(table);
        initialize();
        this.c3a = c3a;
        this.table = table;
        for (C3aInst inst : this.c3a.listeInst) {
            inst.accept(this);
        }
    }

    private void initialize() {
        nasm.ajouteInst(new NasmCall(null, new NasmLabel("main"), ""));
        NasmRegister reg_eax = nasm.newRegister();
        reg_eax.colorRegister(Nasm.REG_EAX);
        NasmRegister reg_ebx = nasm.newRegister();
        reg_ebx.colorRegister(Nasm.REG_EBX);

        nasm.ajouteInst(new NasmMov(null, reg_ebx, new NasmConstant(0), " valeur de retour du programme"));
        nasm.ajouteInst(new NasmMov(null, reg_eax, new NasmConstant(1), ""));
        nasm.ajouteInst(new NasmInt(null, ""));
    }

    @Override
    public NasmOperand visit(C3aInstAdd inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);
        NasmOperand dest = inst.result.accept(this);
        nasm.ajouteInst(new NasmMov(label, dest, op1, ""));
        nasm.ajouteInst(new NasmAdd(null, dest, op2, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstCall inst) {

        final String foncName = inst.op1.val.getIdentif();

        NasmRegister reg_esp = nasm.newRegister();
        reg_esp.colorRegister(Nasm.REG_ESP);
        nasm.ajouteInst(new NasmSub(null, reg_esp, new NasmConstant(4), "allocation mémoire pour la valeur de retour"));
        int num = ((C3aTemp) inst.result).num;
        nasm.ajouteInst(new NasmCall(null, new NasmLabel(foncName), ""));

        nasm.ajouteInst(new NasmPop(null, new NasmRegister(num), "récupération de la valeur de retour"));

        if(inst.op1.val.nbArgs > 0)
            nasm.ajouteInst(new NasmAdd(null, reg_esp, new NasmConstant(inst.op1.val.nbArgs * 4), "désallocation des arguments"));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstFBegin inst) {

        local = table.getTableLocale(inst.val.getIdentif());
        NasmLabel label = new NasmLabel(inst.val.getIdentif());
        NasmRegister reg_ebp = nasm.newRegister();
        reg_ebp.colorRegister(Nasm.REG_EBP);
        nasm.ajouteInst(new NasmPush(label, reg_ebp, "sauvegarde la valeur de ebp"));

        NasmRegister reg_esp = nasm.newRegister();
        reg_esp.colorRegister(Nasm.REG_ESP);
        nasm.ajouteInst(new NasmMov(null, reg_ebp, reg_esp, "nouvelle valeur de ebp"));

        int nbVar = local.nbVar();
        nasm.ajouteInst(new NasmSub(null, reg_esp, new NasmConstant(4 * nbVar), "allocation des variables locales"));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInst inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfLess inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);

        NasmOperand result = inst.result.accept(this);

        if (op1 instanceof NasmConstant && op2 instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(label, register, op1, "JumpIfLess 1"));
            nasm.ajouteInst(new NasmCmp(null, register, op2, "on passe par un registre temporaire"));
        } else {
            nasm.ajouteInst(new NasmCmp(label, op1, op2, "JumpIfLess 1"));
        }
        nasm.ajouteInst(new NasmJl(null, result, "JumpIfLess 2"));


        return null;
    }

    @Override
    public NasmOperand visit(C3aInstMult inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);
        NasmOperand dest = inst.result.accept(this);
        nasm.ajouteInst(new NasmMov(label, dest, op1, ""));
        nasm.ajouteInst(new NasmMul(null, dest, op2, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstRead inst) {
        return null;
    }

    @Override
    public NasmOperand visit(C3aInstSub inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);
        NasmOperand dest = inst.result.accept(this);
        nasm.ajouteInst(new NasmMov(label, dest, op1, ""));
        nasm.ajouteInst(new NasmSub(null, dest, op2, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstAffect inst) {

        NasmOperand label = inst.label == null ? null : inst.label.accept(this);
        NasmOperand res = inst.result.accept(this);

        NasmOperand op1 = inst.op1.accept(this);
        nasm.ajouteInst(new NasmMov(label, res, op1, "Affect"));

        return null;
    }


    @Override
    public NasmOperand visit(C3aInstDiv inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand result = inst.result.accept(this);
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);

        NasmRegister reg_eax = nasm.newRegister();
        reg_eax.colorRegister(Nasm.REG_EAX);

        nasm.ajouteInst(new NasmMov(label, reg_eax, op1, ""));

        if (op2 instanceof NasmRegister) {
            System.out.println(((NasmRegister) op2).val);
            int num = ((C3aTemp) inst.op2).num;
            NasmRegister register = new NasmRegister(num);
            nasm.ajouteInst(new NasmDiv(null, register, ""));
        } else if (op2 instanceof NasmConstant) {
            NasmRegister reg_ebx = nasm.newRegister();
            reg_ebx.colorRegister(Nasm.REG_EBX);
            nasm.ajouteInst(new NasmMov(null, reg_ebx, op2, ""));
            nasm.ajouteInst(new NasmDiv(null, reg_ebx, ""));
        }
        nasm.ajouteInst(new NasmMov(null, result, reg_eax, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstFEnd inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmRegister reg_esp = nasm.newRegister();
        reg_esp.colorRegister(Nasm.REG_ESP);
        nasm.ajouteInst(new NasmAdd(label, reg_esp, new NasmConstant(4 * local.nbVar()), "désallocation des variables locales"));

        NasmRegister reg_ebp = nasm.newRegister();
        reg_ebp.colorRegister(Nasm.REG_EBP);
        nasm.ajouteInst(new NasmPop(null, reg_ebp, "restaure la valeur de ebp"));
        nasm.ajouteInst(new NasmRet(null, ""));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfEqual inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);

        NasmOperand result = inst.result.accept(this);

        if (op1 instanceof NasmConstant && op2 instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(label, register, op1, "JumpIfEqual 1"));
            nasm.ajouteInst(new NasmCmp(null, register, op2, "on passe par un registre temporaire"));
        } else {
            nasm.ajouteInst(new NasmCmp(label, op1, op2, "JumpIfEqual 1"));
        }
        nasm.ajouteInst(new NasmJe(null, result, "JumpIfEqual 2"));


        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJumpIfNotEqual inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op1 = inst.op1.accept(this);
        NasmOperand op2 = inst.op2.accept(this);

        NasmOperand result = inst.result.accept(this);

        if (op1 instanceof NasmConstant && op2 instanceof NasmConstant) {
            NasmRegister register = nasm.newRegister();
            nasm.ajouteInst(new NasmMov(label, register, op1, "JumpIfNotEqual 1"));
            nasm.ajouteInst(new NasmCmp(null, register, op2, "on passe par un registre temporaire"));
        } else {
            nasm.ajouteInst(new NasmCmp(label, op1, op2, "JumpIfNotEqual 1"));
        }
        nasm.ajouteInst(new NasmJne(null, result, "JumpIfNotEqual 2"));


        return null;
    }

    @Override
    public NasmOperand visit(C3aInstJump inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand result = inst.result.accept(this);
        nasm.ajouteInst(new NasmJmp(label, result, "Jump"));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstParam inst) {

        NasmOperand op = inst.op1.accept(this);

        nasm.ajouteInst(new NasmPush(null, op, "Param"));

        return null;
    }

    @Override
    public NasmOperand visit(C3aInstReturn inst) {

        NasmOperand label = inst.label != null ? inst.label.accept(this) : null;
        NasmOperand op = inst.op1.accept(this);

        NasmRegister ebp_reg = nasm.newRegister();
        ebp_reg.colorRegister(Nasm.REG_EBP);

        NasmAddress addr = new NasmAddress(ebp_reg, '+', new NasmConstant(2));

        nasm.ajouteInst(new NasmMov(label, addr, op, "ecriture de la valeur de retour"));

        return null;

    }

    @Override
    public NasmOperand visit(C3aInstWrite inst) {

        NasmOperand label = inst.label == null ? null : inst.label.accept(this);
        NasmOperand op1 = inst.op1.accept(this);

        NasmRegister reg_eax = nasm.newRegister();
        reg_eax.colorRegister(Nasm.REG_EAX);
        nasm.ajouteInst(new NasmMov(label, reg_eax, op1, "Write 1"));

        nasm.ajouteInst(new NasmCall(null, new NasmLabel("iprintLF"), "Write 2"));

        return null;
    }

    @Override
    public NasmOperand visit(C3aConstant oper) {
        return new NasmConstant(oper.val);
    }

    @Override
    public NasmOperand visit(C3aLabel oper) {
        return new NasmLabel("l" + oper.getNumber());
    }

    @Override
    public NasmOperand visit(C3aTemp oper) {
        return new NasmRegister(oper.num);
    }

    @Override
    public NasmOperand visit(C3aVar oper) {

        NasmRegister reg_ebp = nasm.newRegister();
        reg_ebp.colorRegister(Nasm.REG_EBP);

        TsItemVar localVar = local.getVar(oper.item.getIdentif());
        if (localVar != null && local != table) {
            if (localVar.isParam) {
                int varNum = (8 + (4 * local.nbArg()) - (4 * oper.item.adresse)) / 4;
                return new NasmAddress(reg_ebp, '+', new NasmConstant(varNum));
            } else {
                int varNum =oper.item.adresse + 1;
                return new NasmAddress(reg_ebp, '-', new NasmConstant(varNum));
            }
        } else if (oper.index == null) { //It's Not An Array
            return new NasmAddress(new NasmLabel(oper.item.getIdentif()));
        }

        NasmOperand op = oper.index.accept(this);
        return new NasmAddress(new NasmLabel(oper.item.getIdentif()), '+', op);
    }

    @Override
    public NasmOperand visit(C3aFunction oper) {
        return null;
    }

    public Nasm getNasm() {
        return nasm;
    }
}
