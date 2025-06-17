package me.xjqsh.lrtactical.client.renderer.model;

import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.FunctionalBedrockPart;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

import static com.tacz.guns.client.model.GunModelConstant.*;

public class CustomBedrockModel extends BedrockAnimatedModel {
    private static final Pattern FIRSTPERSON_EFFECT_PATTERN = Pattern.compile("^1p_effect(_(\\d+))?$");
    private boolean effectVisible = false;

    public CustomBedrockModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        // 左手手臂
        this.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(this));
        // 右手手臂
        this.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(this));

        for (Map.Entry<String, ModelRendererWrapper> entry : modelMap.entrySet()) {
            if (FIRSTPERSON_EFFECT_PATTERN.matcher(entry.getKey()).find()) {
                if (entry.getValue().getModelRenderer() instanceof FunctionalBedrockPart functionalPart) {
                    functionalPart.functionalRenderer = this::renderEffect;
                }
            }
        }
    }

    @Nullable
    private IFunctionalRenderer renderEffect(BedrockPart part) {
        part.visible = effectVisible;
        return null;
    }

    public void setEffectVisible(boolean visible) {
        this.effectVisible = visible;
    }
}
