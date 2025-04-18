import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        Set<String> aktivneStoritve = new HashSet<>();
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

        Map<String, String> tripRoute = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/trips.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(aktivneStoritve.contains(values[1])) {
                    tripRoute.put(values[2], values[0]);
                }
            }
        }

        Map<String, String> routeNames = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/routes.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if(!tripRoute.containsValue(values[0])) continue;

                routeNames.put(values[0], values[2]);
            }
        }

        Map<String, List<LocalTime>> casiZaPosameznoLinijo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/stop_times.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(!values[3].equals(stop_id) || !tripRoute.containsKey(values[0])) continue;

                LocalTime arrival_time = LocalTime.parse(values[1]);
                if(arrival_time.isBefore(trenutnaUra) || arrival_time.isAfter(cezDveUri)) continue;

                String route_id = tripRoute.get(values[0]);
                casiZaPosameznoLinijo.putIfAbsent(routeNames.get(route_id),new ArrayList<>());
                casiZaPosameznoLinijo.get(routeNames.get(route_id)).add(arrival_time);
            }
        }
    }
}