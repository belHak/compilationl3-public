package ig;

import fg.*;
import nasm.*;
import util.graph.*;
import util.intset.*;

import java.io.*;
import java.util.Arrays;

public class Ig {
    public Graph graph;
    public FgSolution fgs;
    public int regNb;
    public Nasm nasm;
    public Node int2Node[];
    public ColorGraph cg;


    public Ig(FgSolution fgs) {
        this.fgs = fgs;
        this.graph = new Graph();
        this.nasm = fgs.nasm;
        this.regNb = this.nasm.getTempCounter();
        this.int2Node = new Node[regNb];
        this.construction();
        cg = new ColorGraph(graph, 4, getPrecoloredTemporaries());
        cg.coloration();
    }

    public void construction() {

        for (int index = 0; index < int2Node.length; index++) {
            int2Node[index] = graph.newNode();

        }
        for (NasmInst inst : nasm.listeInst) {

            IntSet in = fgs.in.get(inst);
            IntSet out = fgs.out.get(inst);
            for (int i = 0; i < in.getSize(); i++) {
                for (int j = i + 1; j < in.getSize(); j++) {
                    if (in.isMember(i) && in.isMember(j))
                        graph.addNOEdge(int2Node[i], int2Node[j]);
                }
            }

            for (int i = 0; i < out.getSize(); i++) {
                for (int j = i + 1; j < out.getSize(); j++) {
                    if (out.isMember(i) && out.isMember(j))
                        graph.addNOEdge(int2Node[i], int2Node[j]);
                }
            }

        }
    }

    public int[] getPrecoloredTemporaries() {
        int[] couleurs = new int[regNb];

        Arrays.fill(couleurs, -1);

        for (NasmInst inst : nasm.listeInst) {
            NasmOperand destination = inst.destination;
            NasmOperand source = inst.source;
            getPrecoloredRegister(couleurs, destination);

            getPrecoloredRegister(couleurs, source);


        }
        return couleurs;
    }

    private void getPrecoloredRegister(int[] couleurs, NasmOperand source) {
        if (source != null) {
            if (source.isGeneralRegister()) {
                NasmRegister reg = (NasmRegister) source;
                if (reg.color != Nasm.REG_UNK
                        && reg.color != Nasm.REG_ESP
                        && reg.color != Nasm.REG_EBP)
                    couleurs[reg.val] = reg.color;

            } else if (source instanceof NasmAddress) {
                NasmAddress adress = (NasmAddress) source;
                if (adress.base.isGeneralRegister() && adress.base != null) {
                    NasmRegister reg = (NasmRegister) adress.base;
                    if (reg.color != Nasm.REG_UNK
                            && reg.color != Nasm.REG_ESP
                            && reg.color != Nasm.REG_EBP)
                        couleurs[reg.val] = reg.color;
                }
                if (adress.offset != null) {
                    if (adress.offset.isGeneralRegister()) {
                        NasmRegister reg = (NasmRegister) adress.offset;
                        if (reg.color != Nasm.REG_UNK
                                && reg.color != Nasm.REG_ESP
                                && reg.color != Nasm.REG_EBP)
                            couleurs[reg.val] = reg.color;
                    }
                }
            }
        }
    }


    public void allocateRegisters() {

        for (NasmInst inst : nasm.listeInst) {
            NasmOperand source = inst.source;
            NasmOperand destination = inst.destination;

            colorRegister(source);
            colorRegister(destination);
        }
    }

    private void colorRegister(NasmOperand source) {
        if (source != null) {
            if (source.isGeneralRegister()) {
                NasmRegister regSource = (NasmRegister) source;
                if (regSource.color == Nasm.REG_UNK)
                    regSource.color = cg.couleur[regSource.val];
            } else if (source instanceof NasmAddress) {
                NasmAddress adress = (NasmAddress) source;
                if (adress.base.isGeneralRegister() && adress.base != null) {
                    NasmRegister regSourceBase = (NasmRegister) adress.base;
                    if (regSourceBase.color == Nasm.REG_UNK)
                        regSourceBase.color = cg.couleur[regSourceBase.val];
                }
                if (adress.offset != null) {
                    if (adress.offset.isGeneralRegister()) {
                        NasmRegister regSourceOff = (NasmRegister) adress.offset;
                        if (regSourceOff.color == Nasm.REG_UNK)
                            regSourceOff.color = cg.couleur[regSourceOff.val];
                    }
                }
            }
        }
    }


    public void affiche(String baseFileName) {
        String fileName;
        PrintStream out = System.out;

        if (baseFileName != null) {
            try {
                baseFileName = baseFileName;
                fileName = baseFileName + ".ig";
                out = new PrintStream(fileName);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        for (int i = 0; i < regNb; i++) {
            Node n = this.int2Node[i];
            out.print(n + " : ( ");
            for (NodeList q = n.succ(); q != null; q = q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println(")");
        }
    }
}
	    
    

    
    
