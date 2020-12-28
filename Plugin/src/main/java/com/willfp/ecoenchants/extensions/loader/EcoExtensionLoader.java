package com.willfp.ecoenchants.extensions.loader;

import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.extensions.Extension;
import com.willfp.ecoenchants.extensions.MalformedExtensionException;
import com.willfp.ecoenchants.util.internal.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Concrete implementation of {@link ExtensionLoader}
 */
public class EcoExtensionLoader implements ExtensionLoader {
    private final Set<Extension> extensions = new HashSet<>();

    @Override
    public void loadExtensions() {
        File dir = new File(EcoEnchantsPlugin.getInstance().getDataFolder(), "/extensions");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] extensionJars = dir.listFiles();

        if (extensionJars == null)
            return;

        for (File extensionJar : extensionJars) {
            if (!extensionJar.isFile()) continue;

            try {
                loadExtension(extensionJar);
            } catch (MalformedExtensionException e) {
                Logger.error(extensionJar.getName() + " caused MalformedExtensionException: " + e.getMessage());
            }
        }
    }

    private void loadExtension(File extensionJar) throws MalformedExtensionException {
        URL url = null;
        try {
            url = extensionJar.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ClassLoader cl = new URLClassLoader(new URL[]{url}, EcoEnchantsPlugin.class.getClassLoader());

        InputStream ymlIn = cl.getResourceAsStream("extension.yml");

        if (ymlIn == null) {
            throw new MalformedExtensionException("No extension.yml found in " + extensionJar.getName());
        }

        YamlConfiguration extensionYml = YamlConfiguration.loadConfiguration(new InputStreamReader(ymlIn));

        Set<String> keys = extensionYml.getKeys(false);
        ArrayList<String> required = new ArrayList<>(Arrays.asList("main", "name", "version"));
        required.removeAll(keys);
        if (!required.isEmpty()) {
            throw new MalformedExtensionException("Invalid extension.yml found in " + extensionJar.getName() + " - Missing: " + String.join(", ", required));
        }

        String mainClass = extensionYml.getString("main");
        String name = extensionYml.getString("name");
        String version = extensionYml.getString("version");
        Extension.ExtensionMetadata metadata = new Extension.ExtensionMetadata(version, name);

        Class<?> cls;
        Object object = null;
        try {
            cls = cl.loadClass(mainClass);
            object = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (!(object instanceof Extension))
            throw new MalformedExtensionException(extensionJar.getName() + " is invalid");

        Extension extension = (Extension) object;
        extension.setMetadata(metadata);
        extension.enable();
        extensions.add(extension);
    }

    @Override
    public void unloadExtensions() {
        extensions.forEach(Extension::disable);
        extensions.clear();
    }

    @Override
    public void reloadExtensions() {
        unloadExtensions();
        loadExtensions();
    }

    @Override
    public Set<Extension> getLoadedExtensions() {
        return extensions;
    }
}