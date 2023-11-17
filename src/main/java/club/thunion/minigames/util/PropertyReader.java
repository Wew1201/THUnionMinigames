package club.thunion.minigames.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Properties;

public class PropertyReader {
    private final Properties properties;

    public PropertyReader(Properties properties) {
        this.properties = properties;
    }

    public boolean hasKey(String key) {
        return properties.getProperty(key) != null;
    }

    public String readString(String key) {
        return properties.getProperty(key);
    }

    public int readInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public double readDouble(String key) {
        return Double.parseDouble(properties.getProperty(key));
    }

    public Box readBox(String key) {
        double x, X, y, Y, z, Z;
        x = Double.parseDouble(properties.getProperty(key + ".x"));
        X = Double.parseDouble(properties.getProperty(key + ".X"));
        y = Double.parseDouble(properties.getProperty(key + ".y"));
        Y = Double.parseDouble(properties.getProperty(key + ".Y"));
        z = Double.parseDouble(properties.getProperty(key + ".z"));
        Z = Double.parseDouble(properties.getProperty(key + ".Z"));
        return new Box(x, y, z, X, Y, Z);
    }

    public BlockBox readBlockBox(String key) {
        int x, X, y, Y, z, Z;
        x = Integer.parseInt(properties.getProperty(key + ".x"));
        X = Integer.parseInt(properties.getProperty(key + ".X"));
        y = Integer.parseInt(properties.getProperty(key + ".y"));
        Y = Integer.parseInt(properties.getProperty(key + ".Y"));
        z = Integer.parseInt(properties.getProperty(key + ".z"));
        Z = Integer.parseInt(properties.getProperty(key + ".Z"));
        return new BlockBox(x, y, z, X, Y, Z);
    }

    public Vec3d readVec3d(String key) {
        double x, y, z;
        x = Double.parseDouble(properties.getProperty(key + ".x"));
        y = Double.parseDouble(properties.getProperty(key + ".y"));
        z = Double.parseDouble(properties.getProperty(key + ".z"));
        return new Vec3d(x, y, z);
    }
}
