package util.graph;

import util.graph.*;
import util.intset.*;

import java.sql.Array;
import java.util.*;
import java.io.*;

public class ColorGraph {
    public Graph G;
    public int R;
    public int K;
    private Stack<Integer> pile;
    public IntSet enleves;
    public IntSet deborde;
    public int[] couleur;
    public Node[] int2Node;
    static int NOCOLOR = -1;

    public ColorGraph(Graph G, int K, int[] phi) {
        this.G = G;
        this.K = K;
        pile = new Stack<Integer>();
        R = G.nodeCount();
        couleur = new int[R];
        enleves = new IntSet(R);
        deborde = new IntSet(R);
        int2Node = G.nodeArray();
        for (int v = 0; v < R; v++) {
            int preColor = phi[v];
            if (preColor >= 0 && preColor < K)
                couleur[v] = phi[v];
            else
                couleur[v] = NOCOLOR;
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* associe une couleur à tous les sommets se trouvant dans la pile */
    /*-------------------------------------------------------------------------------------------------------------*/

    public void selection() {


        while(pile.size() != 0){
            int s = pile.pop();
            IntSet voisins = couleursVoisins(s);
            int counter = 0 ;
            for(int index = 0 ; index < voisins.getSize(); index++){
                if(voisins.isMember(index))
                    counter++ ;
            }
            if(counter != K){



                couleur[s] = choisisCouleur(voisins);



            }
        }


    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* récupère les couleurs des voisins de t */
    /*-------------------------------------------------------------------------------------------------------------*/

    public IntSet couleursVoisins(int t) {
        IntSet voisins = new IntSet(K);
        NodeList voisinList = int2Node[t].succs;
        while(voisinList != null){
            Node head =voisinList.head ;
            for(int index = 0 ; index < int2Node.length ; index++){
                if(int2Node[index].equals(head))
                    if(couleur[index] != -1)
                        voisins.add(couleur[index]);

            }
            voisinList = voisinList.tail;
        }
        return voisins ;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* recherche une couleur absente de colorSet */
    /*-------------------------------------------------------------------------------------------------------------*/

    public int choisisCouleur(IntSet voisins) {
        IntSet c = new IntSet(K);

        for(int index = 0 ; index < c.getSize() ; index++){
            c.add(index);
        }

        IntSet cop  = c.minus(voisins).copy();

        for(int index = 0 ; index < cop.getSize() ; index++){
            if(cop.isMember(index)) {
                return index ;
            }
        }
        return -1 ;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* calcule le nombre de voisins du sommet t */
    /*-------------------------------------------------------------------------------------------------------------*/

    public int nbVoisins(int t) {
        return int2Node[t].outDegree();
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* simplifie le graphe d'interférence g                                                                        */
    /* la simplification consiste à enlever du graphe les temporaires qui ont moins de k voisins                   */
    /* et à les mettre dans une pile                                                                               */
    /* à la fin du processus, le graphe peut ne pas être vide, il s'agit des temporaires qui ont au moins k voisin */
    /*-------------------------------------------------------------------------------------------------------------*/

    public int simplification() {

        boolean modif = true;
        int n = couleur.length - Collections.frequency(Arrays.asList(couleur),-1);

        while (pile.size() != n && modif ) {
            modif = false;
            for (int index = 0; index < int2Node.length; index++) {
                if (enleves.isMember(index))
                    continue;
                if (nbVoisins(index) < K && couleur[index] == NOCOLOR) {
                    pile.push(index);
                    enleves.add(index);
                    modif = true ;
                }

            }
        }


        return -1;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------*/

    public void debordement() {

        while( pile.size() != R ){
            for(int index = 0 ; index < R ; index++){
                if(!enleves.isMember(index)){
                    pile.push(index);
                    deborde.add(index);
                    enleves.add(index);
                    simplification();
                    break;
                }
            }

        }
    }


    /*-------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------*/

    public void coloration() {
        this.simplification();
        this.debordement();
        this.selection();
    }

    void affiche() {
        System.out.println("vertex\tcolor");
        for (int i = 0; i < R; i++) {
            System.out.println(i + "\t" + couleur[i]);
        }
    }


}
