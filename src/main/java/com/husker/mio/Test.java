package com.husker.mio;

import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.DownloadingProcess;
import com.husker.mio.processes.UnzippingProcess;
import com.husker.mio.processes.ZippingProcess;

public class Test {

    public static void main(String[] args){
        try {
            new ZippingProcess("Minecraft With VR").addProgressListener(e -> System.out.println(e.getPercent())).startSync();
            new DeletingProcess("Minecraft With VR").addProgressListener(e -> System.out.println(e.getPercent())).startSync();
            new UnzippingProcess("Minecraft With VR.zip").addProgressListener(e -> System.out.println(e.getPercent())).startSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
