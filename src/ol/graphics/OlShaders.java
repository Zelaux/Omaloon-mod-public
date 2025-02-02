package ol.graphics;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;

import mindustry.*;
import mindustry.graphics.*;
import mindustry.type.*;

import ol.*;

import static mindustry.Vars.*;
import static arc.Core.*;

public class OlShaders {
    public static @Nullable OlSurfaceShader dalanite;
    public static PlanetTextureShader planetTextureShader;

    public static void load() {
        dalanite = new OlSurfaceShader("dalanite");
        planetTextureShader = new PlanetTextureShader();
    }

    public static class PlanetTextureShader extends OlLoadShader {
        public Vec3 lightDir = new Vec3(1, 1, 1).nor();
        public Color ambientColor = Color.white.cpy();
        public Vec3 camDir = new Vec3();
        public Planet planet;

        public PlanetTextureShader(){
            super("circle-mesh", "circle-mesh");
        }

        @Override
        public void apply(){
            camDir.set(renderer.planets.cam.direction).rotate(Vec3.Y, planet.getRotation());

            setUniformf("u_lightdir",     lightDir);
            setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
            setUniformf("u_camdir",       camDir);
            setUniformf("u_campos",       renderer.planets.cam.position);
        }
    }

    public static class OlSurfaceShader extends Shader{
        Texture noiseTex;

        public OlSurfaceShader(String frag){
            super(
                    files.internal("shaders/screenspace.vert"),
                    tree.get("shaders/" + frag + ".frag")
            );

            loadNoise();
        }

        public String textureName(){
            return "noise";
        }

        public void loadNoise(){
            assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = texture -> {
                texture.setFilter(Texture.TextureFilter.linear);
                texture.setWrap(Texture.TextureWrap.repeat);
            };
        }

        @Override
        public void apply() {
            setUniformf("u_campos",     camera.position.x - camera.width / 2, camera.position.y - camera.height / 2);
            setUniformf("u_resolution", camera.width, camera.height);
            setUniformf("u_time",        Time.time);

            if(hasUniform("u_noise")) {
                if(noiseTex == null) {
                    noiseTex = assets.get("sprites/" + textureName() + ".png", Texture.class);
                }

                noiseTex.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }
        }
    }
    public static class OlLoadShader extends Shader {
        public OlLoadShader(String fragment, String vertex) {
            super(
                    load("" + vertex + ".vert"),
                    load("" + fragment + ".frag")
            );
        }

        public static Fi load(String path) {
            Fi tree = Vars.tree.get("shaders/"+path);

            return tree.exists() ? tree : OlVars.modInfo.root.child("shaders").findAll(file ->
                    file.name().equals(path)).first();
        }

        public void set(){
            Draw.shader(this);
        }

        @Override
        public void apply() {
            super.apply();

            setUniformf("u_time_millis", System.currentTimeMillis() / 1000f * 60f);
        }
    }
}
