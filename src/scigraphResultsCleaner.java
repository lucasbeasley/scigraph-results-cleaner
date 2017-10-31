import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Lucas Beasley
 * 10/29/17
 * Cleaning up formatted Scigraph Annotations (text files).
 */
public class scigraphResultsCleaner {
    public static class Annotation{
        private String term = "";           //term in paper
        private String id = "";             //GO:ID for ontology term
        private String ref = "";            //ontology term
        private int startIndex = -1;        //term's starting index in paper
        private int endIndex = -1;          //term's ending index in paper
        private String myTerm = "";         //term found in paper via getWord

        //getters/setters
        public void setTerm(String term){ this.term = term; }
        public void setID(String id){ this.id = id; }
        private void setRef(String ref){ this.ref = ref; }
        private void setStartIndex(int start){ this.startIndex = start; }
        private void setEndIndex(int end){ this.endIndex = end; }
        private void setMyTerm(String term){ this.myTerm = term; }
        public String getTerm(){ return this.term; }
        public String getID(){ return this.id; }
        private String getRef(){ return this.ref; }
        private int getStartIndex(){ return this.startIndex; }
        private int getEndIndex(){ return this.endIndex; }
        private String getMyTerm(){ return this.myTerm; }
    }

    public static void main(String[] args) {
        //input directory
        File inDirec = new File("scigraphAnnotations");
        //output directory
        File outDirec = new File("output");
        String filename;

        for(File f: inDirec.listFiles()){
            filename = f.getName();
            filename = filename.substring(0, filename.length()-4);
            //get annotations from annotation file and sort them by end index; check word in paper against indexes
            List<Annotation> annotations = pullAndSort(f);
            //write new annotations out to .tsv files
            writeOut(filename, annotations, outDirec);
        }

    }

    /***
     * getWord returns the term from the text
     * @param beginInd - index that the term begins at
     * @param endInd - index that the term ends at
     * @param text - text to search through
     * @return indexed term in text
     */
    private static String getWord(int beginInd, int endInd, String text){
        String inText;
        inText = text.substring(beginInd, endInd);
        return inText;
    }

    /***
     * pullAndSort pulls annotations from the annotation file and sorts by the ending index
     * @param filen - annotation file
     * @return list of sorted annotations
     */
    private static List<Annotation> pullAndSort(File filen){
        List<Annotation> annos = new ArrayList<>();
        Annotation tempAnno;
        String start, end, text = "", file = "rawtext/"+filen.getName(), term, line;
        int starting, ending;
        Scanner scan;

        try{
            scan = new Scanner(new File(file));
            //pull in text from paper; *if line is a blank line, disregard*
            while(scan.hasNextLine()){
                line = scan.nextLine();
                if(!line.equals("")){
                    text += line + " ";
                }
            }

            scan = new Scanner(filen);
            //skip headers line
            scan.nextLine();
            //pull annotations; *blank lines exist at the end of some annotation files so also check for next value*
            while(scan.hasNextLine() && scan.hasNext()){
                tempAnno = new Annotation();
                //pull annotation values and assign to a temp annotation
                start = scan.next();
                starting = Integer.parseInt(start);
                tempAnno.setStartIndex(starting);
                end = scan.next();
                ending = Integer.parseInt(end);
                tempAnno.setEndIndex(ending);
                tempAnno.setID(scan.next());
                tempAnno.setRef(scan.next());
                //*term is surrounded by double quotes; substring for removal*
                term = scan.nextLine();
                term = term.substring(term.indexOf('"')+1, term.length()-1);
                tempAnno.setTerm(term);
                //check indexes against paper for inspection of Term vs. MyTerm
                tempAnno.setMyTerm(getWord(starting, ending, text));
                annos.add(tempAnno);
            }
            //sort by ending indexes
            Collections.sort(annos, Comparator.comparing(Annotation::getEndIndex));
        }catch (FileNotFoundException ex){
            System.out.println("Error: File not found");
            System.out.println("File: " + filen);
        }
        return annos;
    }

    /***
     * writeOut writes annotations out to a tab-separated file (.tsv)
     * @param filename - name of file to write to
     * @param annos - list of annotations for the current file
     * @param outDirectory - directory to write the .tsv file to
     */
    private static void writeOut(String filename, List<Annotation> annos, File outDirectory){
        //setup .tsv file
        filename = outDirectory + "/" + filename + ".tsv";
        File filen = new File(filename);

        //write out to file
        try (PrintWriter writer = new PrintWriter(filen)){
            writer.println("StartIndex\tEndIndex\tGO:ID\tTerm\tOntologyTerm\tMyTerm");
            for(Annotation a : annos) {
                writer.println(a.getStartIndex() + "\t" + a.getEndIndex() + "\t" + a.getID() + "\t" +
                        a.getTerm() + "\t" + a.getRef() + "\t" + a.getMyTerm());
            }
            writer.close();
        }catch (FileNotFoundException ex){
            System.out.println("Error: File not found");
            System.out.println("File: " + filename);
        }
    }
}
