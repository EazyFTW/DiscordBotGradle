package com.eazyftw.gradlediscordplugin.extensions;

import com.eazyftw.gradlediscordplugin.GradleDiscordPlugin;
import com.eazyftw.gradlediscordplugin.util.Color;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MetaExtension {

    public String name, version, jdaVersion, mainClass;
    public String[] repositories, dependencies, relocations;

    public boolean validate() {
        ValidVersionResponse type = versionType(jdaVersion);

        if (name == null) {
            GradleDiscordPlugin.log("Could not find a 'meta' section with a 'name' field in your build.gradle");
            GradleDiscordPlugin.log();
            GradleDiscordPlugin.log(Color.RED + "Please check the GitHub page of GradleDiscordPlugin for more information");
            GradleDiscordPlugin.log();

            return true;
        } else if (mainClass == null) {
            GradleDiscordPlugin.log("Could not find a 'meta' section with a 'mainClass' field in your build.gradle");
            GradleDiscordPlugin.log();
            GradleDiscordPlugin.log(Color.RED + "Please check the GitHub page of GradleDiscordPlugin for more information");
            GradleDiscordPlugin.log();

            return true;
        } else if (version == null) {
            GradleDiscordPlugin.log("Could not find a 'meta' section with a 'version' field in your build.gradle");
            GradleDiscordPlugin.log();
            GradleDiscordPlugin.log(Color.RED + "Please check the GitHub page of GradleDiscordPlugin for more information!");
            GradleDiscordPlugin.log(Color.RESET.toString());

            return true;
        } else if (jdaVersion == null) {
            GradleDiscordPlugin.log(Color.RED + "Could not find a 'meta' section with a 'jdaVersion' field in your build.gradle");
            GradleDiscordPlugin.log();
            GradleDiscordPlugin.log(Color.RED + "Please check the GitHub page of GradleDiscordPlugin for more information!");
            GradleDiscordPlugin.log(Color.RESET.toString());

            return true;
        } else if (type == ValidVersionResponse.NOT_VALID) {
            GradleDiscordPlugin.log(Color.RED + "The 'jdaVersion' specified (" + jdaVersion + ") is not a valid DiscordJDA version.");
            GradleDiscordPlugin.log();
            GradleDiscordPlugin.log(Color.RED + "Please check the GitHub page of GradleDiscordPlugin for more information!");
            GradleDiscordPlugin.log(Color.RESET.toString());

            return true;
        }

        if(type == ValidVersionResponse.VALID) {
            GradleDiscordPlugin.log(Color.YELLOW + "You're not using the latest DiscordJDA version! Find out the version below:");
            GradleDiscordPlugin.log(Color.YELLOW + "https://ci.dv8tion.net/job/JDA/lastSuccessfulBuild/");
            GradleDiscordPlugin.log(Color.RESET.toString());
        }

        return false;
    }

    public static ValidVersionResponse versionType(String version) {
        if(version == null)
            return ValidVersionResponse.NOT_VALID;

        try {
            URL url1 = new URL("https://ci.dv8tion.net/job/JDA/lastSuccessfulBuild/artifact/build/libs/JDA-" + version + ".jar");
            HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
            con1.setRequestMethod("GET");
            con1.connect();

            if (con1.getResponseCode() == 200) {
                return ValidVersionResponse.LATEST;
            } else {
                if(!version.contains("_") || version.split("_").length != 2 || version.split("_")[1].isEmpty())
                    return ValidVersionResponse.NOT_VALID;

                String intVersion = version.split("_")[1];

                URL url2 = new URL("https://ci.dv8tion.net/job/JDA/" + intVersion + "/artifact/build/libs/JDA-" + version + ".jar");
                HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
                con2.setRequestMethod("GET");
                con2.connect();

                return con2.getResponseCode() == 200 ? ValidVersionResponse.VALID :  ValidVersionResponse.NOT_VALID;
            }
        } catch (IOException ex) {
            GradleDiscordPlugin.log(Color.RED + "Failed to check if 'jdaVersion' (" + version + ") is a valid DiscordJDA version.");

            return ValidVersionResponse.NOT_VALID;
        }
    }

    public enum ValidVersionResponse {

        LATEST,
        VALID,
        NOT_VALID;

    }
}
