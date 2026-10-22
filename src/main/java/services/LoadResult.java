package services;

import java.util.List;

public record LoadResult<T>(List<T> items, int skippedLines) {
}
