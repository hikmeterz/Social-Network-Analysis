package Q1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main{

    public static void main(String[] args) {
        Map<Integer, Set<Integer>> collaborationGraph = loadCollaborationGraph("../CA-HepPh.txt");
        Set<Integer> giantComponent = findGiantComponent(collaborationGraph);
        Map<Integer, Integer> histogram = computeDistancesHistogram(giantComponent, collaborationGraph);
        writeHistogramToFile(histogram, "output.txt");
    }

    private static Map<Integer, Set<Integer>> loadCollaborationGraph(String filePath) {
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    int from = Integer.parseInt(parts[0]);
                    int to = Integer.parseInt(parts[1]);
                    graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
                    graph.computeIfAbsent(to, k -> new HashSet<>()).add(from);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    private static Set<Integer> findGiantComponent(Map<Integer, Set<Integer>> graph) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> largestComponent = new HashSet<>();

        for (Integer node : graph.keySet()) {
            if (!visited.contains(node)) {
                Set<Integer> component = bfs(graph, node, visited);
                if (component.size() > largestComponent.size()) {
                    largestComponent = component;
                }
            }
        }

        return largestComponent;
    }

    private static Set<Integer> bfs(Map<Integer, Set<Integer>> graph, Integer start, Set<Integer> visited) {
        Set<Integer> component = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Integer node = queue.poll();
            component.add(node);

            for (Integer neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return component;
    }

    private static Map<Integer, Integer> computeDistancesHistogram(Set<Integer> giantComponent, Map<Integer, Set<Integer>> graph) {
        Map<Integer, Integer> histogram = new HashMap<>();
        Map<Integer, Integer> distances;

        for (Integer node : giantComponent) {
            distances = new HashMap<>();
            computeDistancesFromNode(graph, node, distances, giantComponent);
            for (Integer distance : distances.values()) {
                if (distance > 0) { // Exclude self-distance
                    histogram.put(distance, histogram.getOrDefault(distance, 0) + 1);
                }
            }
        }

        //Adjust for double counting
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            entry.setValue(entry.getValue() / 2);
        }

        return histogram;
    }

    private static void computeDistancesFromNode(Map<Integer, Set<Integer>> graph, Integer startNode, Map<Integer, Integer> distances, Set<Integer> giantComponent) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startNode);
        distances.put(startNode, 0);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            for (Integer neighbor : graph.get(current)) {
                if (giantComponent.contains(neighbor) && !distances.containsKey(neighbor)) {
                    distances.put(neighbor, distances.get(current) + 1);
                    queue.add(neighbor);
                }
            }
        }
    }

    private static void writeHistogramToFile(Map<Integer, Integer> histogram, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            //Sort the distances
            List<Integer> sortedDistances = new ArrayList<>(histogram.keySet());
            Collections.sort(sortedDistances);
    
            //Write the distances and counts to the file in the specified format
            for (Integer distance : sortedDistances) {
                writer.write("Distance " + distance + ": " + histogram.get(distance));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
