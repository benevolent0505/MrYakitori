package yakitori.model;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.Observable;
import java.util.Properties;

/**
 * Created by benevolent0505 on 15/05/17.
 */
public class Credentials extends Observable {
    private String consumerKey;
    private String consumerSecret;

    private static Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;

    private boolean isAuthorized;

    public Credentials() {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        Properties consumer = loadTwitter4jProperties();
        consumerKey = consumer.getProperty("oauth.consumerKey");
        consumerSecret = consumer.getProperty("oauth.consumerSecret");
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);

        if (isAuthorized()) {
            accessToken = loadAccessToken();
            cb.setOAuthAccessToken(accessToken.getToken());
            cb.setOAuthAccessTokenSecret(accessToken.getTokenSecret());

            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();
        } else {
            try {
                twitter = TwitterFactory.getSingleton();
                requestToken = twitter.getOAuthRequestToken();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }

    public void getAccessToken(String pin) {
        try {
            if (pin.length() > 0) {
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } else {
                accessToken = twitter.getOAuthAccessToken(requestToken);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        storeAccessToken();
        setAuthorized(true);
    }

    public String getAuthorizationURL() {
        if (requestToken != null) {
            return requestToken.getAuthorizationURL();
        } else {
            return null;
        }
    }

    public static Twitter getTwitter() {
        return twitter;
    }

    public boolean isAuthorized() {
        if (loadAccessToken() != null) {
            return true;
        } else {
            return false;
        }
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        setChanged();
        notifyObservers();
    }

    private void storeAccessToken() {
        File file = new File("twitter4j.properties");
        Properties properties = new Properties();
        InputStream is = null;
        OutputStream os = null;
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                properties.load(is);
                is.close();

                properties.setProperty("oauth.accessToken", accessToken.getToken());
                properties.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());
                os = new FileOutputStream(file);
                properties.store(os, "twitter4j.properties");
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Properties loadTwitter4jProperties() {
        File file = new File("twitter4j.properties");
        Properties properties = new Properties();
        InputStream is = null;
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                properties.load(is);
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return properties;
    }

    private AccessToken loadAccessToken() {
        File file = new File("twitter4j.properties");
        Properties properties = new Properties();
        InputStream is = null;
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                properties.load(is);
                is.close();

                if (properties.getProperty("oauth.accessToken") != null ||
                        properties.getProperty("oauth.accessTokenSecret") != null) {
                    AccessToken token = new AccessToken(properties.getProperty("oauth.accessToken"),
                            properties.getProperty("oauth.accessTokenSecret"));
                    return token;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
