import com.fasterxml.jackson.databind.ObjectMapper;
import models.DownloadData;
import models.Opportunity;
import models.Pair;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        // Cerinta 1
        final String filePath = "src/test/resources/downloads.txt";
        final List<DownloadData> downloads =  parseDownloadsFile(filePath);

        // Cerinta 3
        final Pair mostPopularShow = findMostFrequentShowIdWithCity(downloads, "San Francisco");
        System.out.println("Most popular show is: " + mostPopularShow.getId() );
        System.out.println("Number of downloads is: " + mostPopularShow.getCount() );
        Assert.assertEquals(mostPopularShow.getId(), "Who Trolled Amber");
        Assert.assertEquals(mostPopularShow.getCount(), 24);

        // Cerinta 4
        final Pair mostPopularDevice = findMostFrequentDeviceType(downloads);
        System.out.println("Most popular device is:  " + mostPopularDevice.getId() );
        System.out.println("Number of downloads is: " + mostPopularDevice.getCount() );
        Assert.assertEquals(mostPopularDevice.getId(), "mobiles & tablets");
        Assert.assertEquals(mostPopularDevice.getCount(), 60);

        // Cerinta 5
        final List<Pair> showsWithPrerollOpp = countPrerollOpportunities(downloads);
        showsWithPrerollOpp.sort(Comparator.comparingLong(Pair::getCount).reversed());
        showsWithPrerollOpp.forEach(  showWithPrerollOpp ->
                System.out.println("Show Id: " + showWithPrerollOpp.getId() + " Preroll Opportunity Number: "+ showWithPrerollOpp.getCount() + "\n"));

        // Cerinta 6
        final Map<String, Set<Long>> showsWithOriginalEventTimes = getShowIdWithOriginalEventTimes(downloads);
        final Map<String, Set<LocalDateTime>> showsWithOriginalLocalDates = convertTimestampsToLocalDateTimes(showsWithOriginalEventTimes);
        final Set<String> weeklyShows = findShowsWithWeeklyHours(showsWithOriginalLocalDates);
        weeklyShows.forEach(show -> {
            if (showsWithOriginalLocalDates.get(show).stream().findFirst().isPresent()) {
                System.out.println("Show Id: " + show + " -  " +
                        showsWithOriginalLocalDates.get(show).stream().findFirst().get().getDayOfWeek() + " " +
                        showsWithOriginalLocalDates.get(show).stream().findFirst().get().getHour() + "\n");
            }
        });

    }

    public static List<DownloadData> parseDownloadsFile(String filePath) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<DownloadData> downloads = new ArrayList<>();

        try {
            final List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (final String line : lines) {
                final DownloadData download = objectMapper.readValue(line, DownloadData.class);
                downloads.add(download);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading the file: " + e.getMessage());
        }
        return downloads;
    }

    public static Pair findMostFrequentShowIdWithCity(final List<DownloadData> downloads, final String targetCity) {
        final Map<String, Long> showIdCounts = downloads.stream()
                .filter(download -> targetCity.equalsIgnoreCase(download.getCity()))
                .collect(Collectors.groupingBy(download -> download.getDownloadIdentifier().getShowId(), Collectors.counting()));

        return showIdCounts.entrySet().stream()
                .map(entry -> new Pair(entry.getKey(), entry.getValue()))
                .max(Comparator.comparingLong(Pair::getCount))
                .orElse(null);
    }

    public static Pair findMostFrequentDeviceType(final List<DownloadData> downloads) {
        final Map<String, Long> deviceTypeCounts = downloads.stream()
                .collect(Collectors.groupingBy(DownloadData::getDeviceType, Collectors.counting()));

        return deviceTypeCounts.entrySet().stream()
                .map(entry -> new Pair(entry.getKey(), entry.getValue()))
                .max(Comparator.comparingLong(Pair::getCount))
                .orElse(null);
    }

    public static List<Pair> countPrerollOpportunities(final List<DownloadData> downloads) {
        return downloads.stream()
                .collect(Collectors.groupingBy(
                        download -> download.getDownloadIdentifier().getShowId(),
                        Collectors.summingLong(download -> {
                            final List<String> adBreaks = download.getOpportunities().stream()
                                    .flatMap(opportunity -> opportunity.getPositionUrlSegments().get("aw_0_ais.adBreakIndex").stream())
                                    .collect(Collectors.toList());
                            return adBreaks.stream().filter("preroll"::equals).count();
                        }))
                )
                .entrySet().stream()
                .map(entry -> new Pair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    public static Map<String, Set<Long>> getShowIdWithOriginalEventTimes(final List<DownloadData> downloads) {
        return downloads.stream()
                .collect(Collectors.groupingBy(
                        download -> download.getDownloadIdentifier().getShowId(),
                        Collectors.mapping(
                                download -> download.getOpportunities().stream()
                                        .map(Opportunity::getOriginalEventTime)
                                        .findFirst()
                                        .orElse(null),
                                Collectors.toCollection(LinkedHashSet::new)
                        )
                ));
    }

    private static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    public static Map<String, Set<LocalDateTime>> convertTimestampsToLocalDateTimes(final Map<String, Set<Long>> showIdToEventTimes) {
        return showIdToEventTimes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(Objects::nonNull)
                                .map(Main::timestampToLocalDateTime)
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                ));
    }


    public static Set<String> findShowsWithWeeklyHours(Map<String, Set<LocalDateTime>> showIdToLocalDateTimes) {
        final Set<String> showsWithWeeklyGaps = new HashSet<>();

        for (Map.Entry<String, Set<LocalDateTime>> entry : showIdToLocalDateTimes.entrySet()) {
            final String showId = entry.getKey();
            final Set<LocalDateTime> eventTimes = entry.getValue();

            final List<LocalDateTime> sortedEventTimes = eventTimes.stream()
                    .sorted()
                    .collect(Collectors.toList());

            for (int i = 0; i < sortedEventTimes.size(); i++) {
                for (int j = i + 1; j < sortedEventTimes.size(); j++) {
                    if (ChronoUnit.WEEKS.between(sortedEventTimes.get(i), sortedEventTimes.get(j)) >= 1) {
                        showsWithWeeklyGaps.add(showId);
                        break;
                    }
                }
                if (showsWithWeeklyGaps.contains(showId)) {
                    break;
                }
            }
        }

        return showsWithWeeklyGaps;
    }

}
