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

    private void useAndDefForArithmeticOpsExceptDiv(NasmInst inst) {
        NasmOperand destination = inst.destination;
        NasmOperand source = inst.source;

        int useIntSetValue = searchGreatestRegValue(destination,source);
        int defIntSetValue = getGreatestValueForDest(destination);

        IntSet useSet = useIntSetValue == -1 ? null : new IntSet(useIntSetValue + 1);
        IntSet defSet = defIntSetValue == -1 ? null : new IntSet(defIntSetValue + 1);

        if(useSet == null){
            emptyUseOrDef(inst,use);
        }

        if(defSet == null){
            emptyUseOrDef(inst,def);
        }

        if( useSet != null && defSet != null) {
            if (destination instanceof NasmAddress) {
                NasmAddress add = ((NasmAddress) destination);

                if (add.base != null && add.base.isGeneralRegister()) {
                    NasmRegister reg = ((NasmRegister) add.base);
                    useSet.add(reg.val);
                    defSet.add(reg.val);
                }

                if (add.offset != null && add.offset.isGeneralRegister()) {
                    NasmRegister reg = ((NasmRegister) add.offset);
                    useSet.add(reg.val);
                    defSet.add(reg.val);
                }
            } else {
                if (destination.isGeneralRegister()) {
                    NasmRegister reg = ((NasmRegister) destination);
                    useSet.add(reg.val);
                    defSet.add(reg.val);
                }
            }
        }

        if(useSet != null) {
            if (source instanceof NasmAddress) {
                add2IntSetARegFromAddr(useSet,  source);
            } else {
                if (source.isGeneralRegister()) {
                    NasmRegister reg = ((NasmRegister) source);
                    useSet.add(reg.val);
                }
            }
        }

        if(useSet != null) use.put(inst, useSet);
        if(defSet != null) def.put(inst, defSet);

    }

    private int searchGreatestRegValue(NasmOperand destination, NasmOperand source) {
        int greatestValueForDest ;
        int greatestValueForSrc ;

        greatestValueForDest = getGreatestValueForDest(destination);

        greatestValueForSrc = getGreatestValueForSrc(source);

        return Math.max(greatestValueForDest, greatestValueForSrc);
    }

    private int getGreatestValueForSrc(NasmOperand source) {
        return getGreatestValueOfRegister(source);
    }

    private int getGreatestValueForDest(NasmOperand destination) {
        return getGreatestValueOfRegister(destination);
    }

    private int getGreatestValueOfRegister(NasmOperand destOrSrc) {
        int greatestValue = -1;
        if (destOrSrc instanceof NasmAddress) {
            NasmAddress add = ((NasmAddress) destOrSrc);
            greatestValue = getResult(add.base, add.offset);
        } else if (destOrSrc.isGeneralRegister()) {
            NasmRegister reg = ((NasmRegister) destOrSrc);
            greatestValue = reg.val;
        }
        return greatestValue;
    }

    private void emptyUseOrDef(NasmInst inst, Map<NasmInst, IntSet> inst2intSet) {
        IntSet intSet = new IntSet(0);
        inst2intSet.put(inst, intSet);
    }

    private int getResult(NasmOperand destination, NasmOperand source) {
        int reg_num_1 = -1;
        int reg_num_2 = -1;


        if (destination != null && destination.isGeneralRegister()) {
            NasmRegister reg_dest = ((NasmRegister) destination);
            reg_num_1 = reg_dest.val;
        }

        if (source != null && source.isGeneralRegister()) {
            NasmRegister reg_source = ((NasmRegister) source);
            reg_num_2 = reg_source.val;
        }

        return Math.max(reg_num_1,reg_num_2);
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

        int useValue = searchGreatestRegValue(dest,src);

        IntSet useSet1 = useValue == -1 ? null : new IntSet(useValue + 1) ;

        if(useSet1 == null) {
            emptyUseOrDef(inst , use);
            return null;
        }
//Peut-être un bogue
        if(dest instanceof NasmAddress){
            add2IntSetARegFromAddr(useSet1,  dest);
        }else{
            if(dest.isGeneralRegister()){
                NasmRegister reg = ((NasmRegister)dest);
                useSet1.add(reg.val);
            }else {
                emptyUseOrDef(inst, use);
            }
        }

        if(src instanceof NasmAddress){
            add2IntSetARegFromAddr(useSet1,  src);
        }else{
            if(src.isGeneralRegister()){
                NasmRegister reg = ((NasmRegister)src);
                useSet1.add(reg.val);
            }else {
                emptyUseOrDef(inst, use);
            }
        }
        use.put(inst,useSet1);


        return null;
    }

    private void addRegInUse(NasmInst inst, NasmOperand op, IntSet intSet,Map<NasmInst, IntSet> inst2intSet) {
        if(op instanceof NasmAddress){
            add2IntSetARegFromAddr(intSet, op);
        }else{
            if(op.isGeneralRegister()){
                NasmRegister reg = ((NasmRegister) op);
                intSet.add(reg.val);
            }else {
                emptyUseOrDef(inst, inst2intSet);
                return;
            }
        }
        inst2intSet.put(inst, intSet);
    }

    private void add2IntSetARegFromAddr(IntSet useSet, NasmOperand dest) {
        NasmAddress add = ((NasmAddress)dest);
        if(add.base != null && add.base.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)add.base);
            useSet.add(reg.val);
        }

        if (add.offset != null && add.offset.isGeneralRegister()){
            NasmRegister reg = ((NasmRegister)add.offset);
            useSet.add(reg.val);
        }
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

        int value = getGreatestValueOfRegister(op);

        IntSet intSet = value == -1 ? null : new IntSet(value + 1 );
        if(intSet != null){
            addRegInUse(inst,op,intSet,inst2intSet);
        }else{
            emptyUseOrDef(inst,inst2intSet);
        }
//
//        if (op.isGeneralRegister()) {
//
//            NasmRegister reg = ((NasmRegister) op);
//            IntSet intSet = new IntSet(reg.val + 1);
//            intSet.add(reg.val);
//            inst2intSet.put(inst, intSet);
//
//        }else {
//            emptyUseOrDef(inst, inst2intSet);
//        }
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

    
