import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Генератор отчетов
 */
public class ReportGen{

    private static int counter = 0;
    public static int width;
    public static int height;
    public static String header ="|";
    public static String stringSeparator = "";
    public static String pageSeparator = "~";

    public static void main(String[] args) throws Exception {
        //получаем имена файлов из аргументов
        String settingsFile = args[0];
        String sourceFile = args[1];
        String reportFile = args[2];

        //СОздаем списки заголовков и параметров
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<Integer> widths = new ArrayList<>();
        //считываем параметры и заголовки из файла xml путем перебора всех элементов файла xml
        XmlRead(settingsFile, titles, widths);

        int[] lengthOfParametrs = new int[widths.size() - 1];
        for (int i = 0; i < lengthOfParametrs.length; i++) {
            //переписываем значения длины строки в массив, начиная с первого элемента
            lengthOfParametrs[i] = widths.get(i + 1);
        }
        //Создание заголовка из списка titles с разделителем
        for (int i = 0; i < titles.size(); i++) {
            header += " " + lineFilling(titles.get(i), lengthOfParametrs[i], ' ') + " |";
        }
        //Создание разделителя строк
        stringSeparator = lineFilling(stringSeparator, width, '-');

        printReportToFile(sourceFile, reportFile, lengthOfParametrs);

    }

    public static void printReportToFile(String sourceFile, String reportFile, int[] lengthOfParametrs) {
        BufferedReader reader;
        BufferedWriter bufferedWriter;
        try {

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-16"));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile)));

            //печатаем заголовок
            bufferedWriter.write(header);
            bufferedWriter.write("\r\n");
            counter++;
            //построчно обрабатываем файл-источник и пишем в файл результата
            while (reader.ready()){
                String line = reader.readLine();

                String[] params = line.split("\\t");
                int maxLines = 0;
                Map<Integer, ArrayList<String>> map = new HashMap<>(params.length);
                for (int i = 0; i < lengthOfParametrs.length; i++) {
                    ArrayList<String> separatedParameter = parameterSeparation(params[i], lengthOfParametrs[i]);
                    map.put(i, separatedParameter);
                    if (maxLines < separatedParameter.size()){
                        maxLines = separatedParameter.size();
                    }
                }
                bufferedWriter.write(stringSeparator);
                bufferedWriter.write("\r\n");
                counter++;
                for (int i = 0; i < maxLines; i++) {
                    String resultLine = "|";
                    for (int j = 0; j < lengthOfParametrs.length; j++) {
                        if (map.get(j).size() <= i){
                            resultLine += " " + lineFilling("", lengthOfParametrs[j], ' ') + " |";
                        }else {
                            resultLine += " " + map.get(j).get(i) + " |";
                        }
                    }
                    printLine(resultLine, bufferedWriter);
                    bufferedWriter.flush();
                }

            }
            reader.close();
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //заполняем списки параметров из файла .xml
    public static void XmlRead(String settingsFile, ArrayList<String> titles, ArrayList<Integer> widths) {
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(settingsFile, new FileInputStream(settingsFile));
            while (xmlReader.hasNext()) {
                xmlReader.next();
                if (xmlReader.isStartElement()) {
                    //если имя поля совпадает c width, то добавляем в список widths
                    if (xmlReader.getLocalName().equals("width")) {
                        xmlReader.next();
                        widths.add(Integer.parseInt(xmlReader.getText()));
                    } else if (xmlReader.getLocalName().equals("title")) {
                        xmlReader.next();
                        titles.add(xmlReader.getText());
                    } else if (xmlReader.getLocalName().equals("height")) {
                        xmlReader.next();
                        height = Integer.parseInt(xmlReader.getText());
                    }
                }

            }
            xmlReader.close();

        }catch (FileNotFoundException | XMLStreamException e){
            e.printStackTrace();
            System.out.println("Не найден файл .xml");
        }
        //Получаем длину строки
        width = widths.get(0);
    }


    //заполнение строки символом до заданной длины
    public static String lineFilling(String str, int limit, char ch){
        while (str.length() < limit){
            str += ch;
        }

        return str;
    }

//разделение строки на части не более заданной длины
    public static ArrayList<String> parameterSeparation(String parameter, int limit){
        ArrayList<String> result = new ArrayList<>();
        if (parameter.contains("/")){
            if (parameter.length() > limit){
                result.add(lineFilling(parameter.substring(0, parameter.length() - 4), limit, ' '));
                result.add(lineFilling(parameter.substring(parameter.length() - 4), limit, ' '));
            }
            else {
                result.add(lineFilling(parameter, limit, ' '));
            }
        }else {
            String[] words = parameter.split(" ");
            for (String word : words){
                String balance = word;
                while (balance.length() > limit) {
                    result.add(balance.substring(0, limit));
                    balance = balance.substring(limit);
                }
                if (balance.length() > 0){
                    result.add(lineFilling(balance, limit, ' '));
                }
            }
        }
        return result;
    }

//запись строки в файл
    public static void printLine(String resultLine, BufferedWriter writer) throws IOException{
        if (counter == height){
            writer.write(pageSeparator);
            writer.write("\r\n");
            counter = 0;
        }
        if (counter == 0){
            writer.write(header);
            writer.write("\r\n");
            counter++;
            writer.write(stringSeparator);
            writer.write("\r\n");
            counter++;
        }
        writer.write(resultLine);
        writer.write("\r\n");
        counter++;
    }
}