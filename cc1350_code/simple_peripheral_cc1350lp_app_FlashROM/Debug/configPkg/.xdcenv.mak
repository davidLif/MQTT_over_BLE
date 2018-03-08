#
_XDCBUILDCOUNT = 
ifneq (,$(findstring path,$(_USEXDCENV_)))
override XDCPATH = C:/Texas_industries/SimpleLink_cc13/simplelink_cc13x0_sdk_1_60_00_21/source;C:/Texas_industries/SimpleLink_cc13/simplelink_cc13x0_sdk_1_60_00_21/kernel/tirtos/packages;C:/Texas_industries/ccsv7/ccs_base
override XDCROOT = C:/Texas_industries/xdctools_3_50_04_43_core
override XDCBUILDCFG = ./config.bld
endif
ifneq (,$(findstring args,$(_USEXDCENV_)))
override XDCARGS = 
override XDCTARGETS = 
endif
#
ifeq (0,1)
PKGPATH = C:/Texas_industries/SimpleLink_cc13/simplelink_cc13x0_sdk_1_60_00_21/source;C:/Texas_industries/SimpleLink_cc13/simplelink_cc13x0_sdk_1_60_00_21/kernel/tirtos/packages;C:/Texas_industries/ccsv7/ccs_base;C:/Texas_industries/xdctools_3_50_04_43_core/packages;..
HOSTOS = Windows
endif
