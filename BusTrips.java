import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BusTrips {

    public static boolean jePozitivenInt(String argument) {
        try {
            return Integer.parseInt(argument) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        if(
                args.length != 3 ||
                !jePozitivenInt(args[0]) ||
                !jePozitivenInt(args[1]) ||
                !args[2].equalsIgnoreCase("absolute") && !args[2].equalsIgnoreCase("relative")
        ) {
            System.out.println("Uporaba: BusTrips <id postaje> <število avtobusov> <absolute|relative>");
            return;
        }

        String stop_id = args[0];
        int max_bus = Integer.parseInt(args[1]);
        String oblikaCasa = args[2];

        // =========== ZA REALEN ČAS ==================
        /*LocalDate danes = LocalDate.now();
        LocalTime trenutnaUra = LocalTime.now();*/
        // ============================================

        // =========== ZA TESTIRANJE ==================
        LocalDate danes = LocalDate.of(2020, 3, 15);
        LocalTime trenutnaUra = LocalTime.of(14, 15);
        // ============================================

        LocalTime cezDveUri = trenutnaUra.plusHours(2);
        int stevilkaDnevaVTednu = danes.getDayOfWeek().getValue();

        // ============== PRIDOBIVANJE AKTIVNIH STORITEV ===================
        Set<String> aktivneStoritve = new HashSet<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/calendar.txt"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                LocalDate startDate = LocalDate.parse(values[8], dateFormatter);
                LocalDate endDate = LocalDate.parse(values[9], dateFormatter);
                if(danes.isBefore(startDate) || danes.isAfter(endDate)) continue;

                if(values[stevilkaDnevaVTednu].equals("1")){
                    String serviceId = values[0];
                    aktivneStoritve.add(serviceId);
                }
            }
        }

        // ============== PRIDOBIVANJE KATERI POT POTEKA NA KATERI LINIJI ===================
        Map<String, String> tripRoute = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/trips.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String serviceID = values[1];
                if(aktivneStoritve.contains(serviceID)) {
                    String tripID = values[2];
                    String routeID = values[0];
                    tripRoute.put(tripID, routeID);
                }
            }
        }

        // ============== PRIDOBIVANJE POTREBNIH IMEN LINIJ ===================
        Map<String, String> routeNames = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/routes.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");

                String routeID = values[0];
                if(!tripRoute.containsValue(routeID)) continue;

                String routeShortName = values[2];
                routeNames.put(routeID, routeShortName);
            }
        }

        // ============== PRIDOBIVANJE VSEH VELJAVNIH ČASOV ZA POSAMEZNO LINIJO ===================
        Map<String, List<LocalTime>> casiZaPosameznoLinijo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/stop_times.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");

                String stopIdLine = values[3];
                String tripId = values[0];
                if(!stopIdLine.equals(stop_id) || !tripRoute.containsKey(tripId)) continue;

                LocalTime arrival_time = LocalTime.parse(values[1]);
                if(arrival_time.isBefore(trenutnaUra) || arrival_time.isAfter(cezDveUri)) continue;

                String route_id = tripRoute.get(values[0]);
                casiZaPosameznoLinijo.putIfAbsent(routeNames.get(route_id),new ArrayList<>());
                casiZaPosameznoLinijo.get(routeNames.get(route_id)).add(arrival_time);
            }
        }

        // ============== PRIDOBIVANJE IMENA POSTAJALIŠČA ===================
        try(BufferedReader br = new BufferedReader(new FileReader("GTFS/stops.txt"))) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");

                String stopId = values[0];
                if(!stopId.equals(stop_id)) continue;

                String stopName = values[2];
                System.out.println("Postajališče " + stopName);
            }
        }

        // ============== IZPIS VSEGA ===================
        for(String relacija : casiZaPosameznoLinijo.keySet()) {
            List<LocalTime> prihodi = casiZaPosameznoLinijo.get(relacija).stream().sorted().limit(max_bus).toList();

            System.out.print(relacija + ": ");
            for(int i = 0; i < prihodi.size(); i++) {
                if(oblikaCasa.equalsIgnoreCase("absolute")){
                    System.out.print(prihodi.get(i));
                } else {
                    long razlika = Duration.between(trenutnaUra, prihodi.get(i)).toMinutes();
                    System.out.print(razlika + "min");
                }
                if(i < prihodi.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }
    }
}