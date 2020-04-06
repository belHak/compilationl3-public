package fg;

import nasm.*;
import util.graph.Node;
import util.graph.NodeList;
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
    final int MAX_VALUE_REGISTER;

    public FgSolution(Nasm nasm, Fg fg) {
        this.nasm = nasm;
        MAX_VALUE_REGISTER = nasm.getTempCounter();
        this.fg = fg;
        this.use = new HashMap<NasmInst, IntSet>();
        this.def = new HashMap<NasmInst, IntSet>();
        this.in = new HashMap<NasmInst, IntSet>();
        this.out = new HashMap<NasmInst, IntSet>();
        for (NasmInst inst : nasm.listeInst) {
            inst.accept(this);
        }
        // In and Out
        for (int i = 0; i < nasm.listeInst.size(); i++) {
            NasmInst nasmInst = nasm.listeInst.get(i);
            IntSet intSet1 = new IntSet(MAX_VALUE_REGISTER);
            in.put(nasmInst, intSet1);
            IntSet intSet2 = new IntSet(MAX_VALUE_REGISTER);
            out.put(nasmInst, intSet2);
        }

        List<NasmInst> nasmInsts = nasm.listeInst;
        boolean equals;
        do {
            iterNum++;
            Map<NasmInst, IntSet> inP = new HashMap<>();
            Map<NasmInst, IntSet> outP = new HashMap<>();
            for (NasmInst nasmInst : nasmInsts) {
                inP.put(nasmInst, in.get(nasmInst));
                outP.put(nasmInst, out.get(nasmInst));

                IntSet outCopy = out.get(nasmInst).copy();
                IntSet defCopy = def.get(nasmInst).copy();
                IntSet minus = outCopy.minus(defCopy);
                IntSet useCopy = use.get(nasmInst).copy();
                IntSet union = useCopy.union(minus);

                in.put(nasmInst, union);

                Node node = fg.inst2Node.get(nasmInst);
                NodeList nodeList = node.succ();
                List<Node> list = new ArrayList<>();
                while (nodeList != null) {
                    list.add(nodeList.head);
                    nodeList = nodeList.tail;
                }
                List<NasmInst> collectInst = new ArrayList<>();
                for (Node n : list) {
                    collectInst.add(fg.node2Inst.get(n));
                }

                List<IntSet> intSetList = new ArrayList<>();
                for (NasmInst n : collectInst) {
                    intSetList.add(in.get(n).copy());
                }
                if (intSetList.size() < 1) continue;
                IntSet outUnion = intSetList.get(0);
                for (int j = 1; j < intSetList.size(); j++) {
                    outUnion.union(intSetList.get(j));
                }
                out.put(nasmInst, outUnion);
            }

            Set<NasmInst> set = in.keySet();
            equals = true;
            for(NasmInst n : set){
                if(in.containsKey(n) && inP.containsKey(n)){
                    if(!in.get(n).equal(inP.get(n))){
                        equals = false;
                    }
                }else {
                    equals = false;
                }

                if(out.containsKey(n) && outP.containsKey(n)){
                    if(!out.get(n).equal(outP.get(n))){
                        equals = false;
                    }
                }else{
                    equals = false;
                }
            }
        } while (!equals);
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


        IntSet useSet = new IntSet(MAX_VALUE_REGISTER);
        IntSet defSet = new IntSet(MAX_VALUE_REGISTER);


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


        if (source instanceof NasmAddress) {
            add2IntSetARegFromAddr(useSet, source);
        } else {
            if (source.isGeneralRegister()) {
                NasmRegister reg = ((NasmRegister) source);
                useSet.add(reg.val);
            }
        }


        use.put(inst, useSet);
        def.put(inst, defSet);

    }


    private void emptyUseOrDef(NasmInst inst, Map<NasmInst, IntSet> inst2intSet) {
        IntSet intSet = new IntSet(MAX_VALUE_REGISTER);
        inst2intSet.put(inst, intSet);
    }

    @Override
    public Void visit(NasmCall inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmDiv inst) {
        emptyUseOrDef(inst, def);

        IntSet intSet = new IntSet(MAX_VALUE_REGISTER);
        addRegInUse(inst, inst.source, intSet, use);

        return null;
    }

    @Override
    public Void visit(NasmJe inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJle inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJne inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmMul inst) {
        useAndDefForArithmeticOpsExceptDiv(inst);
        return null;
    }

    @Override
    public Void visit(NasmOr inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmCmp inst) {
        emptyUseOrDef(inst, def);
        NasmOperand dest = inst.destination;
        NasmOperand src = inst.source;

        IntSet useSet1 = new IntSet(MAX_VALUE_REGISTER);

        if (dest instanceof NasmAddress) {
            add2IntSetARegFromAddr(useSet1, dest);
        } else if (dest.isGeneralRegister()) {
            NasmRegister reg = ((NasmRegister) dest);
            useSet1.add(reg.val);
        }

        if (src instanceof NasmAddress) {
            add2IntSetARegFromAddr(useSet1, src);
        } else if (src.isGeneralRegister()) {
            NasmRegister reg = ((NasmRegister) src);
            useSet1.add(reg.val);
        }

        use.put(inst, useSet1);


        return null;
    }

    private void addRegInUse(NasmInst inst, NasmOperand op, IntSet intSet, Map<NasmInst, IntSet> inst2intSet) {
        if (op instanceof NasmAddress) {
            add2IntSetARegFromAddr(intSet, op);
        } else {
            if (op.isGeneralRegister()) {
                NasmRegister reg = ((NasmRegister) op);
                intSet.add(reg.val);
            }
        }
        inst2intSet.put(inst, intSet);
    }

    private void add2IntSetARegFromAddr(IntSet useSet, NasmOperand dest) {
        NasmAddress add = ((NasmAddress) dest);
        if (add.base != null && add.base.isGeneralRegister()) {
            NasmRegister reg = ((NasmRegister) add.base);
            useSet.add(reg.val);
        }

        if (add.offset != null && add.offset.isGeneralRegister()) {
            NasmRegister reg = ((NasmRegister) add.offset);
            useSet.add(reg.val);
        }
    }

    @Override
    public Void visit(NasmInst inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJge inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJl inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmNot inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmPop inst) {
        emptyUseOrDef(inst,use);

        IntSet destDef =  new IntSet(MAX_VALUE_REGISTER);

        addRegInUse(inst, inst.destination, destDef, def);

        return null;
    }

    @Override
    public Void visit(NasmRet inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmXor inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmAnd inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJg inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
    }

    @Override
    public Void visit(NasmJmp inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
        return null;
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
        IntSet intSet = new IntSet(MAX_VALUE_REGISTER);
        addRegInUse(inst, op, intSet, inst2intSet);

    }

    @Override
    public Void visit(NasmPush inst) {
        emptyUseOrDef(inst, def);

        IntSet useSet =  new IntSet(MAX_VALUE_REGISTER );

        addRegInUse(inst, inst.source, useSet, use);

        return null;
    }

    @Override
    public Void visit(NasmSub inst) {
        useAndDefForArithmeticOpsExceptDiv(inst);
        return null;
    }

    @Override
    public Void visit(NasmEmpty inst) {
        emptyUseOrDef(inst,use);
        emptyUseOrDef(inst,def);
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

    
