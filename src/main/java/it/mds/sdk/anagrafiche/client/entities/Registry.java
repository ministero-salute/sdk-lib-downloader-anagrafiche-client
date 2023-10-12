package it.mds.sdk.anagrafiche.client.entities;

import it.mds.sdk.anagrafiche.client.DownloaderClient;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Rappresenta un'anagrafica restituita dal downloader.
 * Le classi registry si ottengono tramite DownloaderClient.<br>
 * <br>
 * Questa classe è da ritenersi "in sola lettura",
 * utilizzabile solo quando restituita dal DownloaderClient.
 *
 * @see DownloaderClient
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public class Registry {

    private boolean isNew;
    private Date lastUpdate;
    private Date nextUpdate;
    private String name;
    private RegistryMetadata metadata;
    private Types types;
    private Stream<Datum> data;

    /**
     * Stabilisce se questa anagrafica è una nuova versione.
     *
     * @return Questa property è valorizzata secondo i seguenti criteri:<br>
     *  - 'true':  se l'anagrafica che la contiene è una nuova versione,
     *             rispetto a quelle scaricate prima di questa.<br>
     *  - 'false': se le versioni precedenti di questa anagrafica sono ancora valide.<br>
     * <br>
     *  Nel caso 'isNew' = 'false' NON saranno valorizzate le seguenti altre proprietà di questa classe:
     *  'metadata' e 'data'.<br>
     *  Questo perchè valgono i dati restituiti dalle precedenti versioni dell'anagrafica.<br>
     *  'isNew = false' equivale a una risposta <b>'304'</b>, cioè l'anagrafica <b>non</b> è cambiata rispetto all'ultima richiesta.
     * <br>
     *  Nel caso 'isNew' = 'true' tutte le altre proprietà di questa classe saranno valorizzate.<br>
     *  'isNew = true' equivale a una risposta <b>'200'</b>, cioè l'anagrafica <b>è</b> cambiata rispetto all'ultima richiesta.
     *
     */
    public boolean isNew() {
        return this.isNew;
    }

    /**
     * Data di ultimo aggiornamento dell'anagrafica, da parte del downloader remoto.
     */
    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Data di prossimo aggiornamento dell'anagrafica, da parte del downloader remoto.
     */
    public Date getNextUpdate() {
        return this.nextUpdate;
    }

    /**
     * Nome dell'anagrafica.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Metadata esposti dall'anagrafica.
     */
    public RegistryMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * Tipi di dato per ciascun campo dell'anagrafica
     */
    public Types getTypes() {
        return types;
    }
    /**
     * Stream dei dati veri e propri contenuti nell'anagrafica.
     */
    public Stream<Datum> getData() {
        return this.data;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setNextUpdate(Date nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTypes(Types types) {
        this.types = types;
    }

    public void setMetadata(RegistryMetadata metadata) {
        this.metadata = metadata;
    }

    public void setData(Stream<Datum> data) {
        this.data = data;
    }
}
