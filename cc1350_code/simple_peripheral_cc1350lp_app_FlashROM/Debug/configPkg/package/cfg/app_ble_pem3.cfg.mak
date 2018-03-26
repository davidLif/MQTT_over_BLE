# invoke SourceDir generated makefile for app_ble.pem3
app_ble.pem3: .libraries,app_ble.pem3
.libraries,app_ble.pem3: package/cfg/app_ble_pem3.xdl
	$(MAKE) -f C:\ti\simplelink_cc13x0_sdk_1_50_00_08\examples\rtos\CC1350_LAUNCHXL\blestack\simple_peripheral\tirtos\ccs\config/src/makefile.libs

clean::
	$(MAKE) -f C:\ti\simplelink_cc13x0_sdk_1_50_00_08\examples\rtos\CC1350_LAUNCHXL\blestack\simple_peripheral\tirtos\ccs\config/src/makefile.libs clean

