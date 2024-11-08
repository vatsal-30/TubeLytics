package model;

import org.junit.Before;

public class ChannelProfileModelTest {
    private ChannelProfile channelProfile;

    @Before
    public void setUp() {
        channelProfile = new ChannelProfile("channel-001", "https://example.com/image1.jpg", "channel-001's description", "1000000", "9999");
    }
}
