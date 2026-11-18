# Sourced by the Magisk, KernelSU and APatch installers. Only uses helpers all three provide.
MIN_API=@MIN_API@

if [ "$KSU" = true ]; then
  ui_print "- Root: KernelSU ($KSU_VER_CODE)"
elif [ "$APATCH" = true ]; then
  ui_print "- Root: APatch ($APATCH_VER_CODE)"
else
  ui_print "- Root: Magisk ($MAGISK_VER_CODE)"
fi

[ "$API" -ge "$MIN_API" ] || abort "! Android API $API is too old (needs $MIN_API+)"

ui_print "- Installing @NAME@ as system app"
set_perm_recursive "$MODPATH/system" 0 0 0755 0644
