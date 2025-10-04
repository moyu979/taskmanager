package connectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.*;

public class ADBConnector implements Connector{
    private String ip;
    private String port;

    @JsonIgnore
    private Dimension screenSize;

    @JsonCreator
    public ADBConnector(
            @JsonProperty("ip") String ip,
            @JsonProperty("port") String port) {
        this.ip = ip;
        this.port = port;

        // 反序列化完成后执行初始化逻辑
        loadScreenSize();
    }

    // 初始化方法
    private void loadScreenSize() {
        this.screenSize = new Dimension(1920, 1080);
    }

    
}
