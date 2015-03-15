package net.yura.domination.lobby.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.MapServerClient;
import net.yura.domination.mapstore.MapServerListener;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.domination.mapstore.gen.XMLMapAccess;
import net.yura.lobby.server.GameLobby;
import net.yura.mobile.io.ServiceLink.Task;

/**
 * @author Yura Mamyrin
 */
public class GameSettings implements GameSettingsMXBean {

    private File mapsDir;
    private int mapMaxRes = 677;
    private int mapMaxCountries = 100;

    public GameSettings(File mapsDir) {
        this.mapsDir = mapsDir;
    }

    @Override
    public void setAIWait(int a) {
        AIManager.setWait(a);
    }
    @Override
    public int getAIWait() {
        return AIManager.getWait();
    }

    @Override
    public void setMaxMapResolution(int max) {
        mapMaxRes = max;
    }
    @Override
    public int getMaxMapResolution() {
        return mapMaxRes;
    }

    @Override
    public void setMaxMapCountries(int max) {
        mapMaxCountries = max;
    }
    @Override
    public int getMaxMapCountries() {
        return mapMaxCountries;
    }

    public void updateMaps() {
        // get list of all maps from the server
        List<Map> serverMaps = MapUpdateService.getMaps(MapChooser.MAP_PAGE,Collections.EMPTY_LIST);

        try {
            new XMLMapAccess().save(RiskUtil.streamOpener.saveMapFile("extra_maps.xml"), new Task("maps", serverMaps));
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // get list of lacal maps
        List<String> localMaps = Arrays.asList(mapsDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".map");
            }
        }));

        // decide what maps should be downloaded
        final List<String> mapsToDownload = new ArrayList();
        for (Map map : serverMaps) {
            String mapName = MapChooser.getFileUID( map.getMapUrl() );
            if (!localMaps.contains(mapName) || map.needsUpdate(MapChooser.createMap(mapName).getVersion())) {
                mapsToDownload.add(MapChooser.getURL(MapChooser.getContext(MapChooser.MAP_PAGE), map.getMapUrl()));
            }
        }

        //MapUpdateService service = MapUpdateService.getInstance();
        //service.init(localMaps, MapChooser.MAP_PAGE);
        //List<net.yura.domination.mapstore.Map> mapsToUpdate = service.mapsToUpdate;

        if (!mapsToDownload.isEmpty()) {

            final AtomicReference<MapServerClient> mapServerClient = new AtomicReference();
            final AtomicReference<String> error = new AtomicReference();
            final AtomicInteger downloadCount = new AtomicInteger();
            final Thread thread = Thread.currentThread();

            MapServerClient client = new MapServerClient(new MapServerListener() {
                volatile int count;
                public void gotResultCategories(String url, List categories) { }
                public void gotResultMaps(String url, List maps) { }
                public void onXMLError(String string) { }
                public void downloadFinished(String mapUID) {
                    if (downloadCount.incrementAndGet() == mapsToDownload.size()) {
                        mapServerClient.get().kill();
                    }
                }
                public void onDownloadError(String string) {
                    mapServerClient.get().kill();
                    error.set(string);
                    thread.interrupt();
                }
                public void publishImg(Object key) { }
            });
            mapServerClient.set(client);

            // download the maps
            client.start();
            for (String fullMapUrl: mapsToDownload) {
                client.downloadMap(fullMapUrl);
            }

            // wait for all downloads to finish
            try {
                client.awaitTermination();
            }
            catch(InterruptedException in) {
                throw new RuntimeException(error.get(), in);
            }

            // check for errors
            if (error.get() != null) {
                throw new IllegalStateException(error.get());
            }
            if (!client.getInbox().isEmpty()) {
                throw new IllegalStateException("inbox not empty");
            }
        }

        StringBuilder gameOptions = new StringBuilder("luca.map,ameroki.map,eurasien.map,geoscape.map,lotr.map,risk.map,RiskEurope.map,roman_empire.map,sersom.map,teg.map,tube.map,uk.map,world.map");

        // find the maps that are smaller then max resolution
        for (Map map : serverMaps) {
            String mapName = MapChooser.getFileUID(map.getMapUrl());
            int numCountries = (Integer) RiskUtil.loadInfo(mapName, false).get("countries");
            if (map.getMapWidth() <= mapMaxRes && map.getMapHeight() <= mapMaxRes && numCountries <= mapMaxCountries) {
                gameOptions.append(',').append(mapName);
            }
            else {
                System.out.println("skipping " + mapName + " " + numCountries + " (" + map.getMapWidth() + "x" + map.getMapHeight() + ")");
            }
        }

        // save a list of the file names into the GameType
        GameLobby.getInstance().setGameOptions("Domination", gameOptions.toString());
    }

    ServerRisk getServerGame(int id) {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread thread: threadArray) {
            if (thread instanceof ServerRisk) {
                ServerRisk risk = (ServerRisk) thread;
                if (risk.sgr.getId() == id) {
                    return risk;
                }
            }
        }
        throw new IllegalArgumentException("game "+id+" not found");
    }

    @Override
    public void saveGame(int id) throws Exception {
        ServerRisk risk = getServerGame(id);
        File file = new File("game"+id+".save");
        FileOutputStream fout = new FileOutputStream(file);
        risk.getGame().saveGame(fout);
        fout.close();
    }

    @Override
    public void saveGameLog(int id) throws Exception {
        ServerRisk risk = getServerGame(id);
        File file = new File("game"+id+".log");
        RiskUtil.saveGameLog(file, risk.getGame());
    }

    @Override
    public List<Integer> markFinished() throws Exception {

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        List<Integer> games = new ArrayList();
        for (Thread thread: threadArray) {
            if (thread instanceof ServerRisk) {
                ServerRisk risk =(ServerRisk)thread;

                if (!risk.sgr.isFinished() && risk.getGame().getState() == RiskGame.STATE_GAME_OVER) {
                    risk.sgr.gameFinished("ERROR");
                    games.add(risk.sgr.getId());
                }
            }
        }
        return games;
    }
}
