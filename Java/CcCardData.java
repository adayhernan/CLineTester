//Created by ArSi -- https://github.com/arsi-apli

package org.cline;

public class CcCardData {

    private int caid;
    private int providerCount;
    private int[] providers;
    private byte[] nodeid;
    private int[] remoteid;
    private byte[] shareid;
    private byte[] serial;
    private int uphops;

    public void setCaId(int caid) {
        this.caid = caid;
    }

    public byte[] getNodeId() {
        return nodeid;
    }

    public void setRemoteId(int[] remoteid) {
        this.remoteid = remoteid;
    }

    public byte[] getShareId() {
        return shareid;
    }

    public void setUpHops(int uphops) {
        this.uphops = uphops;
    }

    public byte[] getSerial() {
        return serial;
    }

    public int getNumberOfProviders() {
        return providerCount;
    }

    /*

  public void setProfileMatch(boolean a)
  {
    a.g = a;
  }

  public boolean getProfileMatch()
  {
    return a.g;
  }
     */
    public void setShareId(byte[] shareid) {
        this.shareid = shareid;
    }

    public void setNodeId(byte[] nodeid) {
        this.nodeid = nodeid;
    }

    public int[] getRemoteId() {
        return remoteid;
    }

    public void setNumberOfProviders(int providerCount) {
        this.providerCount = providerCount;
    }

    public int[] getProviders() {
        return providers;
    }

    public int getProvider(int nb) {
        return providers[nb];
    }

    public boolean isMatchingPair(int caid, int provider) {
        if (this.caid == caid) {
            for (int i = 0; i < providerCount; i++) {
                if (providers[i] == provider) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
  public String getProviderString()
  {
    Iterator localIterator = a.H.iterator();

    String str = "";

    while (localIterator.hasNext())
    {
      ccProvider localccProvider = (ccProvider)localIterator.next();

      str = DESUtil.intsToStringShort(localccProvider.getProvider());

      if (localIterator.hasNext())
        str = XmlStringBuffer.ALLATORIxDEMO("L");
    }
  }
     */
    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

    public int getUpHops() {
        return uphops;
    }

    public void setProviders(int[] providers) {
        this.providers = providers;
    }

    public int getCaId() {
        return caid;
    }

}
