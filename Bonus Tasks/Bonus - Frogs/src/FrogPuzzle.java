import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class FrogPuzzle {

    private static final char LEFT_FROG = '<';
    private static final char RIGHT_FROG = '>';
    private static final char EMPTY = '_';
    private static final int N = 4; // Заменете това с входната стойност на N

    public static void main(String[] args) {
        String start = new String(new char[N]).replace("\0", Character.toString(RIGHT_FROG))
                + EMPTY
                + new String(new char[N]).replace("\0", Character.toString(LEFT_FROG));
        String goal = new String(new char[N]).replace("\0", Character.toString(LEFT_FROG))
                + EMPTY
                + new String(new char[N]).replace("\0", Character.toString(RIGHT_FROG));

        dfs(start, goal, new HashSet<>(), new Stack<>());
    }

    private static void dfs(String current, String goal, Set<String> visited, Stack<String> path) {
        if (visited.contains(current)) {
            return;
        }

        visited.add(current);
        path.push(current);

        if (current.equals(goal)) {
            printPath(path);
            path.pop();
            return;
        }

        int pos = current.indexOf(EMPTY);
        // Проверка за възможно движение на жаби наляво
        if (pos > 0 && current.charAt(pos - 1) == RIGHT_FROG) { // Може да се мести вдясно
            String next = swap(current, pos, pos - 1);
            dfs(next, goal, visited, path);
        }
        if (pos > 1 && current.charAt(pos - 2) == RIGHT_FROG) { // Може да скочи вдясно
            String next = swap(current, pos, pos - 2);
            dfs(next, goal, visited, path);
        }
        // Проверка за възможно движение на жаби надясно
        if (pos < current.length() - 1 && current.charAt(pos + 1) == LEFT_FROG) { // Може да се мести вляво
            String next = swap(current, pos, pos + 1);
            dfs(next, goal, visited, path);
        }
        if (pos < current.length() - 2 && current.charAt(pos + 2) == LEFT_FROG) { // Може да скочи вляво
            String next = swap(current, pos, pos + 2);
            dfs(next, goal, visited, path);
        }

        visited.remove(current);
        path.pop();
    }

    private static String swap(String s, int i, int j) {
        char[] chars = s.toCharArray();
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
        return new String(chars);
    }

    private static void printPath(Stack<String> path) {
        for (String state : path) {
            System.out.println(state);
        }
    }
}
