package userClient;

import java.util.ArrayList;

public class Board {
    private int mID;

    public Board(int id) {
        mID = id;
    }

    private Client mHost;	//房间创建者
    private ArrayList<Client> mClients = new ArrayList<>();		//房间内的所有用户

    public Client getHost() {
        return mHost;
    }
    synchronized public void setHost(Client Host) {
        this.mHost = Host;
        add(mHost);
    }

    public int getID() {
        return mID;
    }

    synchronized public void add(Client client) {
        mClients.add(client);
    }


    /**
     * 把更新发送给组内所有其他成员
     * @param updation
     * @throws Exception
     */
    synchronized public void updateAllMember(String updation) throws Exception {
        int size = mClients.size();
        for(int i = 0; i < size; i++) {
            Client client = mClients.get(i);
            if(client == mHost) continue;	//房间主直接跳过

            client.getPrintWriterRefresh().println(updation + "\n" + "$$END$$");
        }
    }
}
