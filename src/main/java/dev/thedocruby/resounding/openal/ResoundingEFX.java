package dev.thedocruby.resounding.openal;

import dev.thedocruby.resounding.Resounding;
import dev.thedocruby.resounding.ResoundingLog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

import static dev.thedocruby.resounding.config.PrecomputedConfig.pC;

/*
                                        !!!Documentation for OpenAL!!!
                * I am not responsible for anything that happens after you go to these links *
    - ExtEfx(aka Effects Extension) https://github.com/rtpHarry/Sokoban/blob/master/libraries/OpenAL%201.1%20SDK/docs/Effects%20Extension%20Guide.pdf or https://usermanual.wiki/Pdf/Effects20Extension20Guide.90272296/view
    - Core spec(aka OpenAL 1.1 Specification and Reference) https://www.openal.org/documentation/openal-1.1-specification.pdf
    - Core guide(aka OpenAL Programmer's Guide) http://openal.org/documentation/OpenAL_Programmers_Guide.pdf


    Source attributes(2&3): https://www.openal.org/documentation/openal-1.1-specification.pdf#page=34 & http://openal.org/documentation/OpenAL_Programmers_Guide.pdf#page=34
 */

@Environment(EnvType.CLIENT)
public class ResoundingEFX { // TODO: Create separate debug toggle for OpenAl EFX instead of using pC.dLog

    private ResoundingEFX() {}

    private static int[] slots = new int[0];
    private static int[] effects = new int[0];
    private static int[] filters = new int[0];
    private static int directFilter;
    public static boolean efxEnabled = false;
    private static boolean initialized = false;

    public static void setEffect//<editor-fold desc="(Effect_properties)">
    (
            int id,
            float decayTime,
            float density,
            float diffusion,
            float gainHF,
            float decayHFRatio,
            float reflectionsGain,
            float reflectionsDelay,
            float lateReverbGain,
            float lateReverbDelay
    ) //</editor-fold>
    {
        //<editor-fold desc="setReverbParams();">
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_DENSITY, density);
        ResoundingLog.checkErrorLog("Error while assigning \"density\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+density+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_DIFFUSION, diffusion);
        ResoundingLog.checkErrorLog("Error while assigning \"diffusion\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+diffusion+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_GAIN, pC.globalReverbGain);
        ResoundingLog.checkErrorLog("Error while assigning \"gain\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+ pC.globalReverbGain+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_GAINHF, gainHF);
        ResoundingLog.checkErrorLog("Error while assigning \"gainHF\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+gainHF+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_DECAY_TIME, decayTime);
        ResoundingLog.checkErrorLog("Error while assigning \"decayTime\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+decayTime+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, decayHFRatio);
        ResoundingLog.checkErrorLog("Error while assigning \"decayHFRatio\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+decayHFRatio+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, reflectionsGain);
        ResoundingLog.checkErrorLog("Error while assigning \"reflectionsGain\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+reflectionsGain+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, reflectionsDelay);
        ResoundingLog.checkErrorLog("Error while assigning \"reflectionsDelay\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+reflectionsDelay+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, lateReverbGain);
        ResoundingLog.checkErrorLog("Error while assigning \"lateReverbGain\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+lateReverbGain+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, lateReverbDelay);
        ResoundingLog.checkErrorLog("Error while assigning \"lateReverbDelay\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+lateReverbDelay+"\".");
        EXTEfx.alEffectf(effects[id], EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, 1f);
        ResoundingLog.checkErrorLog("Error while assigning \"density\" property to Effect object "+effects[id]+"! Attempted to assign value of \""+1f+"\".");
        //</editor-fold>

        //Attach updated effect object
        EXTEfx.alAuxiliaryEffectSloti(slots[id], EXTEfx.AL_EFFECTSLOT_EFFECT, effects[id]);
        if (!ResoundingLog.checkErrorLog("Error applying Effect object "+effects[id]+" to aux slot "+slots[id]+"!") && pC.dLog){
            Resounding.LOGGER.info("Successfully initialized Effect object {}!", effects[id]);
        }
    }

    public static void setFilter(int id, int sourceID, float gain, float cutoff) {
        // Set reverb send filter values and set source to send to all reverb fx slots
        EXTEfx.alFilterf(filters[id], EXTEfx.AL_LOWPASS_GAIN, gain);
        ResoundingLog.checkErrorLog("Error while assigning \"gain\" property to Effect object "+filters[id]+"! Attempted to assign value of \""+gain+"\".");
        EXTEfx.alFilterf(filters[id], EXTEfx.AL_LOWPASS_GAINHF, cutoff);
        ResoundingLog.checkErrorLog("Error while assigning \"cutoff\" property to Filter object "+filters[id]+"! Attempted to assign value of \""+cutoff+"\".");
        AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, slots[id], 1, filters[id]);
        ResoundingLog.checkErrorLog("Error applying Filter object "+filters[id]+" and aux slot "+slots[id]+" to source "+sourceID+"!");
    }

    private static void deleteAuxiliaryEffectSlots(){       // Remove unused OpenAL Auxiliary Effect slots
        if (pC.dLog) Resounding.LOGGER.info("Removing {} Auxiliary Effect slots...", slots.length);
        EXTEfx.alDeleteAuxiliaryEffectSlots(slots.clone());
        for (int j : slots) {
            if (EXTEfx.alIsAuxiliaryEffectSlot(j)) { Resounding.LOGGER.error("Failed to delete Auxiliary Effect slot {}!", j); }
            else {
                slots = ArrayUtils.removeElement(slots, j);
                if (pC.dLog) { Resounding.LOGGER.info("Auxiliary Effect slot {} deleted.", j); }
            }
        }
    }

    private static void createAuxiliaryEffectSlots(){       // Create new OpenAL Auxiliary Effect slots
        slots = new int[pC.resolution];
        if (pC.dLog) Resounding.LOGGER.info("Creating {} new Auxiliary Effect slots...", pC.resolution);
        EXTEfx.alGenAuxiliaryEffectSlots(slots);
        for(int i = 0; i < pC.resolution; i++) {
            if(EXTEfx.alIsAuxiliaryEffectSlot(slots[i])){
                if (pC.dLog) Resounding.LOGGER.info("Auxiliary Effect slot {} created!", slots[i]);
                EXTEfx.alAuxiliaryEffectSloti(slots[i], EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE); // Set effect type to EAX Reverb
                ResoundingLog.checkErrorLog("Failed to initialize Auxiliary Effect slot "+slots[i]+"!");
            } else { Resounding.LOGGER.error("Failed to create Auxiliary Effect slot! (index {})", i); }
        }
    }

    private static void deleteEffectObjects(){       // Remove unused OpenAL Effect objects
        if (pC.dLog) Resounding.LOGGER.info("Removing {} Effect objects...", effects.length);
        EXTEfx.alDeleteEffects(effects.clone());
        for (int j : effects) {
            if (EXTEfx.alIsEffect(j)) { Resounding.LOGGER.error("Failed to delete Effect object {}!", j); }
            else {
                effects = ArrayUtils.removeElement(effects, j);
                if (pC.dLog) { Resounding.LOGGER.info("Effect object {} deleted.", j); }
            }
        }
    }

    private static void createEffectObjects(){       // Create new OpenAL Effect objects
        effects = new int[pC.resolution];
        if (pC.dLog) Resounding.LOGGER.info("Creating {} new Effect objects...", pC.resolution);
        EXTEfx.alGenEffects(effects);
        for(int i = 0; i < pC.resolution; i++) {
            if(EXTEfx.alIsEffect(effects[i])){
                if (pC.dLog) Resounding.LOGGER.info("Effect object {} created!", effects[i]);
                EXTEfx.alEffecti(effects[i], EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB); // Set effect type to EAX Reverb
                ResoundingLog.checkErrorLog("Failed to initialize Effect object "+effects[i]+"!");
            } else { Resounding.LOGGER.error("Failed to create Effect object! (index {})", i); }
        }
    }

    private static void deleteFilterObjects(){      // Remove unused OpenAL Filter objects
        if (pC.dLog) Resounding.LOGGER.info("Removing {} Filter objects...", filters.length);
        EXTEfx.alDeleteFilters(filters.clone());
        for (int j : filters) {
            if (EXTEfx.alIsFilter(j)) { Resounding.LOGGER.error("Failed to delete Filter object {}!", j); }
            else {
                filters = ArrayUtils.removeElement(filters, j);
                if (pC.dLog) { Resounding.LOGGER.info("Filter object {} deleted.", j); }
            }
        }
    }

    private static void createFilterObjects(){       // Create new OpenAL Filter objects
        filters = new int[pC.resolution];
        if (pC.dLog) Resounding.LOGGER.info("Creating {} new Filter objects...", pC.resolution);
        EXTEfx.alGenFilters(filters);
        for(int i = 0; i < pC.resolution; i++) {
            if(EXTEfx.alIsFilter(filters[i])){
                if (pC.dLog) Resounding.LOGGER.info("Filter object {} created!", filters[i]);
                EXTEfx.alFilteri(filters[i], EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
                ResoundingLog.checkErrorLog("Failed to initialize Filter object "+filters[i]+"!");
            } else { Resounding.LOGGER.error("Failed to create Filter object! (index {})", i); }
        }
    }

    public static void initEAXReverb(){
        if (!efxEnabled || pC.off || initialized) return;
        if (pC.resolution == slots.length) return;

        createAuxiliaryEffectSlots();
        createEffectObjects();
        createFilterObjects();

        for(int i = 0; i < pC.resolution; i++){
            double t = Math.pow((double) i  / pC.resolution, pC.warpFactor);
            double t1 = Math.pow((double)(i + 1) / pC.resolution, pC.warpFactor);
            setEffect(i,
                    (float) Math.max(t1 * 4.142, 0.1),
                    (float) (t1 * 0.5),
                    (float) (0.95 - (pC.reverbCondensationFactor * t1)),
                    (float) (0.95 - (0.75 * t1)),
                    (float) Math.max(0.95 - (0.5 * t1), 0.1),
                    (float) Math.max(Math.pow(1 - t1, 5), 0.1),
                    (float) (t1 * 0.01),
                    (float) (Math.pow(t, 0.2) * 1.618),
                    (float) (t1 * 0.01)
            );
        }
        directFilter = EXTEfx.alGenFilters();
        if(!EXTEfx.alIsFilter(directFilter)) { Resounding.LOGGER.error("Failed to create direct filter object!"); }
        else if(pC.dLog){ Resounding.LOGGER.info("Direct filter object created with ID {}", directFilter); }
        EXTEfx.alFilteri(directFilter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        ResoundingLog.checkErrorLog("Failed to initialize direct filter object!");

        initialized = true;
        Resounding.LOGGER.info("Finished initializing OpenAL Auxiliary Effect slots!");
    }

    public static void setupEXTEfx() {
        //Get current context and device
        final long currentContext = ALC10.alcGetCurrentContext();
        final long currentDevice = ALC10.alcGetContextsDevice(currentContext);
        if (!ALC10.alcIsExtensionPresent(currentDevice, "ALC_EXT_EFX")) {
            Resounding.LOGGER.error("EFX Extension not found on current device, Aborting.");
        }
        Resounding.LOGGER.info("EFX Extension recognized! ");
        efxEnabled = true;
        initEAXReverb();
    }

    public static void cleanUpEXTEfx() {
        deleteAuxiliaryEffectSlots();
        deleteEffectObjects();
        deleteFilterObjects();
        EXTEfx.alDeleteFilters(directFilter);
        if(EXTEfx.alIsFilter(directFilter)) { Resounding.LOGGER.error("Failed to delete direct filter object!"); }
        else if(pC.dLog){ Resounding.LOGGER.info("Direct filter object deleted with ID {}", directFilter); }

        initialized = false;
        efxEnabled = false;
    }

    public static void setDirectFilter(int sourceID, float directGain, float directCutoff) {
        EXTEfx.alFilterf(directFilter, EXTEfx.AL_LOWPASS_GAIN, directGain);
        ResoundingLog.checkErrorLog("Error while assigning \"gain\" property to direct filter object! Attempted to assign value of \""+directGain+"\".");
        EXTEfx.alFilterf(directFilter, EXTEfx.AL_LOWPASS_GAINHF, directCutoff);
        ResoundingLog.checkErrorLog("Error while assigning \"cutoff\" property to direct filter object! Attempted to assign value of \""+directCutoff+"\".");
        AL10.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, directFilter);
        ResoundingLog.checkErrorLog("Error applying direct filter object to source "+sourceID+"!");
    }


    /* public static void setSoundPos(final int sourceID, final Vec3d pos) {
        if (pC.off) return;
        //System.out.println(pos);//TO DO
        AL10.alSourcefv(sourceID, 4100, new float[]{(float) pos.x, (float) pos.y, (float) pos.z});
    } */ // TODO: DirEval
}
