#version 150

uniform vec2 iResolution;
uniform float iTime;
uniform vec4 uGlowColor;
uniform sampler2D uMask;
uniform float uUseMask;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    float aspect = iResolution.x / iResolution.y;
    vec2 d = min(uv, 1.0 - uv);
    d.x *= aspect;
    float edgeDist = min(d.x, d.y);
    float glow = exp(-edgeDist * 20.0) * 0.55;
    float pulse = 0.85 + 0.15 * sin(iTime * 3.0);

    float a = 1.0;
    if (uUseMask > 0.5) {
        a = texture(uMask, uv).a;
    }

    // Shape-following glow: bright inside the texture's alpha and a soft halo just outside it,
    // instead of an always-rectangular square.
    float inside = a * glow;
    float outside = (1.0 - a) * glow * 0.55;
    float shaped = (inside + outside) * pulse;

    fragColor = uGlowColor * shaped;
}
