package gg.pufferfish.pufferfish.flare.collectors;

import co.technove.flare.live.CollectorData;
import co.technove.flare.live.LiveCollector;
import co.technove.flare.live.formatter.SuffixFormatter;
import gg.pufferfish.pufferfish.flare.CustomCategories;
import org.bukkit.Bukkit;

import java.time.Duration;

public class TPSCollector extends LiveCollector {
    private static final CollectorData TPS = new CollectorData("airplane:tps", "TPS", "Ticks per second, or how fast the server updates. For a smooth server this should be a constant 20TPS.", SuffixFormatter.of("TPS"), CustomCategories.MC_PERF);
    private static final CollectorData MSPT = new CollectorData("airplane:mspt", "MSPT", "Milliseconds per tick, which can show how well your server is performing. This value should always be under 50mspt.", SuffixFormatter.of("mspt"), CustomCategories.MC_PERF);

    public TPSCollector() {
        super(TPS, MSPT);

        this.interval = Duration.ofSeconds(5);
    }

    @Override
    public void run() {
        this.report(TPS, Bukkit.getTPS()[0]);
        this.report(MSPT, Bukkit.getAverageTickTime());
    }
}
