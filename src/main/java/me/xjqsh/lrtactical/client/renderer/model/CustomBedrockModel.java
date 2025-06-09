package me.xjqsh.lrtactical.client.renderer.model;

import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;

import java.util.Optional;

import static com.tacz.guns.client.model.GunModelConstant.*;

public class CustomBedrockModel extends BedrockAnimatedModel {
    private static final String EFFECT_NODE = "effect";
    private final BedrockPart effectNode;

    public CustomBedrockModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        // 左手手臂
        this.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(this));
        // 右手手臂
        this.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(this));

        this.effectNode = Optional.ofNullable(modelMap.get(EFFECT_NODE)).map(ModelRendererWrapper::getModelRenderer).orElse(null);
        this.setEffectVisible(false);
    }

    public void setEffectVisible(boolean visible) {
        if (effectNode != null) {
            effectNode.visible = visible;
        }
    }
}
