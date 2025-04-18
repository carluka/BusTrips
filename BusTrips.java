import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class BusTrips {
    public static void main(String[] args) throws IOException {
        if(args.length != 3){
            System.out.println("Uporaba: BusTrips <id postaje> <število naslednjih avtobusov> <oblika časa>");
            return;
        }

        String stop_id = args[0];
        int max_bus = Integer.parseInt(args[1]);
        String oblikaCasa = args[2];

        //LocalDate danes = LocalDate.now();
        //LocalTime trenutnaUra = LocalTime.now();

        // =========== ZA TESTIRANJE ==================
        LocalDate danes = LocalDate.of(2020, 3, 15);
        LocalTime trenutnaUra = LocalTime.of(14, 15);
        // ============================================

        LocalTime cezDveUri = trenutnaUra.plusHours(2);
        int stevilkaDnevaVTednu = danes.getDayOfWeek().getValue();

        Set<String> aktivneStoritve = new HashSet<String>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/calendar.txt"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if(danes.isBefore(LocalDate.parse(values[8], dateFormatter)) || danes.isAfter(LocalDate.parse(values[9], dateFormatter))) continue;

                if(values[stevilkaDnevaVTednu].equals("1")){
                    aktivneStoritve.add(values[0]);
                }
            }
        }


    }
}