package test.srtngcmpny.finger.basic;

public interface IUsbConnState {
    void onUsbConnected();

	void onUsbPermissionDenied();

	void onDeviceNotFound();
}
