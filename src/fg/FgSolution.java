package fg;

import nasm.*;
import util.intset.*;

import java.io.*;
import java.util.*;

public class FgSolution implements NasmVisitor<Void> {
    int iterNum = 0;
    public Nasm nasm;
    Fg fg;
    public Map<NasmInst, IntSet> use;
    public Map<NasmInst, IntSet> def;
    public Map<NasmInst, IntSet> in;
    public Map<NasmInst, IntSet> out;

    public FgSolution(Nasm nasm, Fg fg) {
        this.nasm = nasm;
        this.fg = fg;
        this.use = new HashMap<NasmInst, IntSet>();
        this.def = new HashMap<NasmInst, IntSet>();
        this.in = new HashMap<NasmInst, IntSet>();
        this.out = new HashMap<NasmInst, IntSet>();
        for(NasmInst e : nasm.listeInst){
        	e.accept(this);
		}
    }

    public void affiche(String baseFileName) {
        String fileName;
        PrintStream out = System.out;

        if (baseFileName != null) {
            try {
                baseFileName = baseFileName;
                fileName = baseFileName + ".fgs";
                out = new PrintStream(fileName);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        out.println("iter num = " + iterNum);
        for (NasmInst nasmInst : this.nasm.listeInst) {
            out.println("use = " + this.use.get(nasmInst) + " def = " + this.def.get(nasmInst) + "\tin = " + this.in.get(nasmInst) + "\t \tout = " + this.out.get(nasmInst) + "\t \t" + nasmInst);
        }
    }

    @Override
    public Void visit(NasmAdd inst) {
        useAndDefForArithmeticOpsExceptDiv(inst);
        return null;
    }

    /**
     * Cast Error
     * @param inst
     */
    private void useAndDefForArithmeticOpsExceptDiv(NasmInst inst) {
        NasmOperand destination = inst.destination;
        NasmOperand source = inst.source;

        int result = getResult(destination, source);

        IntSet useSet;
        IntSet defSet;


        useSet = getIntSet( destination, source, result);

        if (useSet == null) {
            emptyUseOrDef(inst, def);
            emptyUseOrDef(inst, use);
            return;
        }

        if(destination.isGeneralRegister()){
            NasmRegister dest = ((NasmRegister) destination);
            useSet.add(dest.val);
            defSet = new IntSet(dest.val + 1);
            defSet.add(dest.val);
            def.put(inst, defSet);
            use.put(inst,useSet);
        }else{
            emptyUseOrDef(inst, def);
        }

        if(source.isGeneralRegister()){
            NasmRegister src = ((NasmRegister) source);
            useSet.add(src.val);
            use.put(inst, useSet);
        }else if(!destination.isGeneralRegister()){
            emptyUseOrDef(inst, use);
        }

    }

    private IntSet getIntSet(NasmOperand destination, NasmOperand source, int result) {
        IntSet useSet;
        if (result > 0) {
            NasmRegister dest = ((NasmRegister)destination);
            useSet = new IntSet(dest.val + 1);
        } else if (result < 0) {
            NasmRegister src = ((NasmRegister)source);
            useSet = new IntSet(src.val + 1);
        }else return null;
        return useSet;
    }

    private void emptyUseOrDef(NasmInst inst, Map<NasmInst, IntSet> inst2intSet) {
        IntSet intSet = new IntSet(0);
        inst2intSet.put(inst, intSet);
    }

    private int getResult(NasmOperand destination, NasmOperand source) {
        int reg_num_1 = -1;
        int reg_num_2 = -1;


        if (destination.isGeneralRegister()) {
            NasmRegister reg_dest = ((NasmRegister) destination);
            reg_num_1 = reg_dest.val;
        }

        if (source.isGeneralRegister()) {
            NasmRegister reg_source = ((NasmRegister) source);
            reg_num_2 = reg_source.val;
        }

        return Objects.compare(reg_num_1, reg_num_2, Comparator.comparingInt(o -> o));
    }

    @Override
    public Void visit(NasmCall inst) {
        emptySet(inst);

        return null;
    }

    @Override
    public Void visit(NasmDiv inst) {
        if (!inst.source.isGeneralRegister()) return null;
        int reg_val = ((NasmRegister) inst.source).val;

        IntSet src_use = new IntSet(reg_val + 1);

        src_use.add(reg_val);
        use.put(inst, src_use);
        return null;
    }

    @Override
    public Void visit(NasmJe inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJle inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJne inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmMul inst) {
        useAndDefForArithmeticOpsExceptDiv(inst);
        return null;
    }

    @Override
    public Void visit(NasmOr inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmCmp inst) {
        emptyUseOrDef(inst,def);
        NasmOperand dest = inst.destination;
        NasmOperand src = inst.source;

        int result = getResult(dest,src);

        IntSet useSet = getIntSet(dest,src, result);

        if(useSet == null){
            emptyUseOrDef(inst,use);
            return null;
        }

        if(dest.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)dest);
            useSet.add(reg.val);
            use.put(inst,useSet);
        }

        if(src.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)src);
            useSet.add(reg.val);
            use.put(inst,useSet);
        }

        return null;
    }

    @Override
    public Void visit(NasmInst inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJge inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJl inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmNot inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmPop inst) {
        emptyUseOrDef(inst,use);
        NasmOperand destination = inst.destination;

        if( destination.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)inst.destination);
            IntSet intSet = new IntSet(reg.val + 1 );
            intSet.add(reg.val);
            def.put(inst,intSet);
        }else {
            emptyUseOrDef(inst,def);
        }
        return null;
    }

    @Override
    public Void visit(NasmRet inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmXor inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmAnd inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJg inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmJmp inst) {
        emptySet(inst);
        return null;
    }

    private void emptySet(NasmInst inst) {
        IntSet useSet = new IntSet(0);
        IntSet defSet = new IntSet(0);

        use.put(inst,useSet);
        def.put(inst,defSet);
    }

    @Override
    public Void visit(NasmMov inst) {

        NasmOperand dest = inst.destination;
        NasmOperand src = inst.source;

        nasmMovSolution(inst, dest, def);
        nasmMovSolution(inst, src, use);

        return null;
    }

    private void nasmMovSolution(NasmMov inst, NasmOperand op, Map<NasmInst, IntSet> inst2intSet) {
        if (op.isGeneralRegister()) {

            NasmRegister reg = ((NasmRegister) op);
            IntSet intSet = new IntSet(reg.val + 1);
            intSet.add(reg.val);
            inst2intSet.put(inst, intSet);

        }else {
            emptyUseOrDef(inst, inst2intSet);
        }
    }

    @Override
    public Void visit(NasmPush inst) {
        emptyUseOrDef(inst,def);
        NasmOperand source = inst.source;

        if( source.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)inst.source);
            IntSet intSet = new IntSet(reg.val + 1 );
            intSet.add(reg.val);
            use.put(inst,intSet);
        }else {
            emptyUseOrDef(inst,use);
        }
        return null;
    }

    @Override
    public Void visit(NasmSub inst) {
        useAndDefForArithmeticOpsExceptDiv(inst);
        return null;
    }

    @Override
    public Void visit(NasmEmpty inst) {
        emptySet(inst);
        return null;
    }

    @Override
    public Void visit(NasmAddress operand) {
        return null;
    }

    @Override
    public Void visit(NasmConstant operand) {
        return null;
    }

    @Override
    public Void visit(NasmLabel operand) {
        return null;
    }

    @Override
    public Void visit(NasmRegister operand) {
        return null;
    }
}

    
