package fg;

import nasm.*;
import util.graph.*;

import java.util.*;
import java.io.*;

public class Fg implements NasmVisitor<Void> {
    public Nasm nasm;
    public Graph graph;
    Map<NasmInst, Node> inst2Node;
    Map<Node, NasmInst> node2Inst;
    Map<String, NasmInst> label2Inst;

    public Fg(Nasm nasm) {
        this.nasm = nasm;
        this.inst2Node = new HashMap<NasmInst, Node>();
        this.node2Inst = new HashMap<Node, NasmInst>();
        this.label2Inst = new HashMap<String, NasmInst>();
        this.graph = new Graph();
        for(NasmInst e : this.nasm.listeInst){
            e.accept(this);
        }

        for(int i = 0 ; i < nasm.listeInst.size() - 1; i++){


            NasmInst instPred = nasm.listeInst.get(i);
            NasmInst instSucc = nasm.listeInst.get(i+1);

            if(isLabeledInstance(instPred)){
                addEdgeToLabel(instPred);
                if (instPred instanceof NasmCall || instPred instanceof NasmJmp) continue;
            }

            Node nodePred = inst2Node.get(instPred);
            Node nodeSucc = inst2Node.get(instSucc);

            graph.addEdge(nodePred,nodeSucc);

        }
    }

    private void addEdgeToLabel(NasmInst inst) {
        String name = inst.address.toString() ;
        if(!label2Inst.containsKey(name)) return;
        NasmInst labeledInst = label2Inst.get(name);

        Node jumpNode = inst2Node.get(inst);
        Node labeledInstNode =  inst2Node.get(labeledInst);

        graph.addEdge(jumpNode,labeledInstNode);
    }

    private boolean isLabeledInstance(NasmInst inst) {
        return inst instanceof NasmJe ||
                inst instanceof NasmJne ||
                inst instanceof NasmJmp ||
                inst instanceof NasmJl ||
                inst instanceof NasmCall;
    }

    public void affiche(String baseFileName) {
        String fileName;
        PrintStream out = System.out;

        if (baseFileName != null) {
            try {
                baseFileName = baseFileName;
                fileName = baseFileName + ".fg";
                out = new PrintStream(fileName);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        for (NasmInst nasmInst : nasm.listeInst) {
            Node n = this.inst2Node.get(nasmInst);
            out.print(n + " : ( ");
            for (NodeList q = n.succ(); q != null; q = q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println(")\t" + nasmInst);
        }
    }

    public Void visit(NasmAdd inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmCall inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmDiv inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJe inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJle inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJne inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmMul inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmOr inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmCmp inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmInst inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJge inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJl inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmNot inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmPop inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmRet inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmXor inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmAnd inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJg inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmJmp inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmMov inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmPush inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmSub inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmEmpty inst) {
        Node n = graph.newNode();
        inst2Node.put(inst,n);
        node2Inst.put(n,inst);
        if(inst.label != null){
            label2Inst.put(inst.label.toString(),inst);
        }
        return null;
    }

    public Void visit(NasmAddress operand) {
        return null;
    }

    public Void visit(NasmConstant operand) {
        return null;
    }

    public Void visit(NasmLabel operand) {
        return null;
    }

    public Void visit(NasmRegister operand) {
        return null;
    }


}
