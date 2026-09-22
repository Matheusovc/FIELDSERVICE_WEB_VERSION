/**
 * FieldService 2.0 - Animated Gradient Background (WebGL2)
 * ---------------------------------------------------------------------------
 * Porte vanilla (sem React) do componente "animated-gradient.tsx".
 * Mantém fielmente o fragment shader, os presets e a matemática de cores.
 * Uso:
 *   FSAnimatedGradient.mount(containerEl, { preset: "Prism", speed: 30 });
 * ou, via atributo no HTML:
 *   <div data-fs-gradient data-preset="Prism"></div>  (auto-init no load)
 */
(function (global) {
  "use strict";

  var PatternShapes = { Checks: 0, Stripes: 1, Edge: 2 };

  var presets = {
    Prism: {
      color1: "#050505", color2: "#66B3FF", color3: "#FFFFFF",
      rotation: -50, proportion: 1, scale: 0.01, speed: 30, distortion: 0,
      swirl: 50, swirlIterations: 16, softness: 47, offset: -299,
      shape: "Checks", shapeSize: 45,
    },
    Lava: {
      color1: "#FF9F21", color2: "#FF0303", color3: "#000000",
      rotation: 114, proportion: 100, scale: 0.52, speed: 30, distortion: 7,
      swirl: 18, swirlIterations: 20, softness: 100, offset: 717,
      shape: "Edge", shapeSize: 12,
    },
    Plasma: {
      color1: "#B566FF", color2: "#000000", color3: "#000000",
      rotation: 0, proportion: 63, scale: 0.75, speed: 30, distortion: 5,
      swirl: 61, swirlIterations: 5, softness: 100, offset: -168,
      shape: "Checks", shapeSize: 28,
    },
    Pulse: {
      color1: "#66FF85", color2: "#000000", color3: "#000000",
      rotation: -167, proportion: 92, scale: 0, speed: 20, distortion: 54,
      swirl: 75, swirlIterations: 3, softness: 28, offset: -813,
      shape: "Checks", shapeSize: 79,
    },
    Vortex: {
      color1: "#000000", color2: "#FFFFFF", color3: "#000000",
      rotation: 50, proportion: 41, scale: 0.4, speed: 20, distortion: 0,
      swirl: 100, swirlIterations: 3, softness: 5, offset: -744,
      shape: "Stripes", shapeSize: 80,
    },
    Mist: {
      color1: "#050505", color2: "#FF66B8", color3: "#050505",
      rotation: 0, proportion: 33, scale: 0.48, speed: 39, distortion: 4,
      swirl: 65, swirlIterations: 5, softness: 100, offset: -235,
      shape: "Edge", shapeSize: 48,
    },
  };

  var VERTEX_SHADER =
    "#version 300 es\n" +
    "in vec4 a_position;\n" +
    "void main() { gl_Position = a_position; }";

  var FRAGMENT_SHADER = "#version 300 es\n" +
"precision highp float;\n" +
"uniform float u_time;\n" +
"uniform float u_pixelRatio;\n" +
"uniform vec2 u_resolution;\n" +
"uniform float u_scale;\n" +
"uniform float u_rotation;\n" +
"uniform vec4 u_color1;\n" +
"uniform vec4 u_color2;\n" +
"uniform vec4 u_color3;\n" +
"uniform float u_proportion;\n" +
"uniform float u_softness;\n" +
"uniform float u_shape;\n" +
"uniform float u_shapeScale;\n" +
"uniform float u_distortion;\n" +
"uniform float u_swirl;\n" +
"uniform float u_swirlIterations;\n" +
"out vec4 fragColor;\n" +
"#define TWO_PI 6.28318530718\n" +
"#define PI 3.14159265358979323846\n" +
"vec2 rotate(vec2 uv, float th) {\n" +
"  return mat2(cos(th), sin(th), -sin(th), cos(th)) * uv;\n" +
"}\n" +
"float random(vec2 st) {\n" +
"  return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);\n" +
"}\n" +
"float noise(vec2 st) {\n" +
"  vec2 i = floor(st);\n" +
"  vec2 f = fract(st);\n" +
"  float a = random(i);\n" +
"  float b = random(i + vec2(1.0, 0.0));\n" +
"  float c = random(i + vec2(0.0, 1.0));\n" +
"  float d = random(i + vec2(1.0, 1.0));\n" +
"  vec2 u = f * f * (3.0 - 2.0 * f);\n" +
"  float x1 = mix(a, b, u.x);\n" +
"  float x2 = mix(c, d, u.x);\n" +
"  return mix(x1, x2, u.y);\n" +
"}\n" +
"vec4 blend_colors(vec4 c1, vec4 c2, vec4 c3, float mixer, float edgesWidth, float edge_blur) {\n" +
"    vec3 color1 = c1.rgb * c1.a;\n" +
"    vec3 color2 = c2.rgb * c2.a;\n" +
"    vec3 color3 = c3.rgb * c3.a;\n" +
"    float r1 = smoothstep(.0 + .35 * edgesWidth, .7 - .35 * edgesWidth + .5 * edge_blur, mixer);\n" +
"    float r2 = smoothstep(.3 + .35 * edgesWidth, 1. - .35 * edgesWidth + edge_blur, mixer);\n" +
"    vec3 blended_color_2 = mix(color1, color2, r1);\n" +
"    float blended_opacity_2 = mix(c1.a, c2.a, r1);\n" +
"    vec3 c = mix(blended_color_2, color3, r2);\n" +
"    float o = mix(blended_opacity_2, c3.a, r2);\n" +
"    return vec4(c, o);\n" +
"}\n" +
"void main() {\n" +
"    vec2 uv = gl_FragCoord.xy / u_resolution.xy;\n" +
"    float t = .5 * u_time;\n" +
"    float noise_scale = .0005 + .006 * u_scale;\n" +
"    uv -= .5;\n" +
"    uv *= (noise_scale * u_resolution);\n" +
"    uv = rotate(uv, u_rotation * .5 * PI);\n" +
"    uv /= u_pixelRatio;\n" +
"    uv += .5;\n" +
"    float n1 = noise(uv * 1. + t);\n" +
"    float n2 = noise(uv * 2. - t);\n" +
"    float angle = n1 * TWO_PI;\n" +
"    uv.x += 4. * u_distortion * n2 * cos(angle);\n" +
"    uv.y += 4. * u_distortion * n2 * sin(angle);\n" +
"    float iterations_number = ceil(clamp(u_swirlIterations, 1., 30.));\n" +
"    for (float i = 1.; i <= iterations_number; i++) {\n" +
"        uv.x += clamp(u_swirl, 0., 2.) / i * cos(t + i * 1.5 * uv.y);\n" +
"        uv.y += clamp(u_swirl, 0., 2.) / i * cos(t + i * 1. * uv.x);\n" +
"    }\n" +
"    float proportion = clamp(u_proportion, 0., 1.);\n" +
"    float shape = 0.;\n" +
"    float mixer = 0.;\n" +
"    if (u_shape < .5) {\n" +
"      vec2 checks_shape_uv = uv * (.5 + 3.5 * u_shapeScale);\n" +
"      shape = .5 + .5 * sin(checks_shape_uv.x) * cos(checks_shape_uv.y);\n" +
"      mixer = shape + .48 * sign(proportion - .5) * pow(abs(proportion - .5), .5);\n" +
"    } else if (u_shape < 1.5) {\n" +
"      vec2 stripes_shape_uv = uv * (.25 + 3. * u_shapeScale);\n" +
"      float f = fract(stripes_shape_uv.y);\n" +
"      shape = smoothstep(.0, .55, f) * smoothstep(1., .45, f);\n" +
"      mixer = shape + .48 * sign(proportion - .5) * pow(abs(proportion - .5), .5);\n" +
"    } else {\n" +
"      float sh = 1. - uv.y;\n" +
"      sh -= .5;\n" +
"      sh /= (noise_scale * u_resolution.y);\n" +
"      sh += .5;\n" +
"      float shape_scaling = .2 * (1. - u_shapeScale);\n" +
"      shape = smoothstep(.45 - shape_scaling, .55 + shape_scaling, sh + .3 * (proportion - .5));\n" +
"      mixer = shape;\n" +
"    }\n" +
"    vec4 color_mix = blend_colors(u_color1, u_color2, u_color3, mixer, 1. - clamp(u_softness, 0., 1.), .01 + .01 * u_scale);\n" +
"    fragColor = vec4(color_mix.rgb, color_mix.a);\n" +
"}\n";

  function hslToRgb(h, s, l) {
    var r, g, b;
    if (s === 0) {
      r = g = b = l;
    } else {
      var hue2rgb = function (p, q, t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1 / 6) return p + (q - p) * 6 * t;
        if (t < 1 / 2) return q;
        if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
        return p;
      };
      var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      var p = 2 * l - q;
      r = hue2rgb(p, q, h + 1 / 3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1 / 3);
    }
    return [r, g, b];
  }

  function hexToRgba(hex) {
    var r = 0, g = 0, b = 0, a = 1, parts;
    if (hex.indexOf("rgba(") === 0) {
      parts = hex.slice(5, -1).split(",");
      r = parseInt(parts[0], 10) / 255;
      g = parseInt(parts[1], 10) / 255;
      b = parseInt(parts[2], 10) / 255;
      a = parseFloat(parts[3]);
    } else if (hex.indexOf("rgb(") === 0) {
      parts = hex.slice(4, -1).split(",");
      r = parseInt(parts[0], 10) / 255;
      g = parseInt(parts[1], 10) / 255;
      b = parseInt(parts[2], 10) / 255;
    } else if (hex.indexOf("hsla(") === 0 || hex.indexOf("hsl(") === 0) {
      var isHsla = hex.indexOf("hsla(") === 0;
      parts = hex.slice(isHsla ? 5 : 4, -1).split(",");
      var h = parseFloat(parts[0]) / 360;
      var s = parseFloat(parts[1]) / 100;
      var l = parseFloat(parts[2]) / 100;
      a = isHsla ? parseFloat(parts[3]) : 1;
      var rgb = hslToRgb(h, s, l);
      r = rgb[0]; g = rgb[1]; b = rgb[2];
    } else if (hex.indexOf("#") === 0) {
      var c = hex.slice(1);
      if (c.length === 3) {
        r = parseInt(c[0] + c[0], 16) / 255;
        g = parseInt(c[1] + c[1], 16) / 255;
        b = parseInt(c[2] + c[2], 16) / 255;
      } else if (c.length >= 6) {
        r = parseInt(c.slice(0, 2), 16) / 255;
        g = parseInt(c.slice(2, 4), 16) / 255;
        b = parseInt(c.slice(4, 6), 16) / 255;
        if (c.length === 8) a = parseInt(c.slice(6, 8), 16) / 255;
      }
    }
    return [r, g, b, a];
  }

  function resolveParams(config) {
    config = config || { preset: "Prism" };
    if (config.preset === "custom") {
      return {
        color1: config.color1, color2: config.color2, color3: config.color3,
        rotation: config.rotation != null ? config.rotation : 0,
        proportion: config.proportion != null ? config.proportion : 35,
        scale: config.scale != null ? config.scale : 1,
        speed: config.speed != null ? config.speed : 25,
        distortion: config.distortion != null ? config.distortion : 12,
        swirl: config.swirl != null ? config.swirl : 80,
        swirlIterations: config.swirlIterations != null ? config.swirlIterations : 10,
        softness: config.softness != null ? config.softness : 100,
        offset: config.offset != null ? config.offset : 0,
        shape: config.shape || "Checks",
        shapeSize: config.shapeSize != null ? config.shapeSize : 10,
      };
    }
    var preset = presets[config.preset] || presets.Prism;
    var out = {};
    for (var k in preset) out[k] = preset[k];
    if (config.speed != null) out.speed = config.speed;
    return out;
  }

  /**
   * Monta o gradiente animado dentro de um container.
   * @param {HTMLElement} container
   * @param {Object} config  { preset } ou { preset:"custom", ... }
   * @returns {Object} handle com destroy()
   */
  function mount(container, config) {
    if (!container) return null;
    var params = resolveParams(config);

    var canvas = document.createElement("canvas");
    canvas.style.display = "block";
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    container.appendChild(canvas);

    var gl = canvas.getContext("webgl2", {
      premultipliedAlpha: true,
      alpha: true,
      antialias: true,
    });

    // Sem WebGL2: aplica um fallback CSS discreto e sai sem erro.
    if (!gl) {
      container.style.background =
        "radial-gradient(circle at 30% 20%, rgba(102,179,255,0.25), transparent 60%), #050505";
      return { destroy: function () { if (canvas.parentNode) canvas.parentNode.removeChild(canvas); } };
    }

    var vertexShader = gl.createShader(gl.VERTEX_SHADER);
    gl.shaderSource(vertexShader, VERTEX_SHADER);
    gl.compileShader(vertexShader);

    var fragmentShader = gl.createShader(gl.FRAGMENT_SHADER);
    gl.shaderSource(fragmentShader, FRAGMENT_SHADER);
    gl.compileShader(fragmentShader);

    var program = gl.createProgram();
    gl.attachShader(program, vertexShader);
    gl.attachShader(program, fragmentShader);
    gl.linkProgram(program);
    gl.useProgram(program);

    var positionBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]),
      gl.STATIC_DRAW
    );

    var positionLocation = gl.getAttribLocation(program, "a_position");
    gl.enableVertexAttribArray(positionLocation);
    gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 0, 0);

    var u = {
      time: gl.getUniformLocation(program, "u_time"),
      resolution: gl.getUniformLocation(program, "u_resolution"),
      pixelRatio: gl.getUniformLocation(program, "u_pixelRatio"),
      scale: gl.getUniformLocation(program, "u_scale"),
      rotation: gl.getUniformLocation(program, "u_rotation"),
      color1: gl.getUniformLocation(program, "u_color1"),
      color2: gl.getUniformLocation(program, "u_color2"),
      color3: gl.getUniformLocation(program, "u_color3"),
      proportion: gl.getUniformLocation(program, "u_proportion"),
      softness: gl.getUniformLocation(program, "u_softness"),
      shape: gl.getUniformLocation(program, "u_shape"),
      shapeScale: gl.getUniformLocation(program, "u_shapeScale"),
      distortion: gl.getUniformLocation(program, "u_distortion"),
      swirl: gl.getUniformLocation(program, "u_swirl"),
      swirlIterations: gl.getUniformLocation(program, "u_swirlIterations"),
    };

    function resize() {
      var width = container.clientWidth;
      var height = container.clientHeight;
      var pixelRatio = window.devicePixelRatio || 1;
      canvas.width = width * pixelRatio;
      canvas.height = height * pixelRatio;
      canvas.style.width = width + "px";
      canvas.style.height = height + "px";
      gl.viewport(0, 0, canvas.width, canvas.height);
    }
    resize();

    var resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(container);

    var startTime = performance.now();
    var frameId;
    var running = true;

    function animate(time) {
      if (!running) return;
      var elapsed = (time - startTime) / 1000;
      var speed = (params.speed / 100) * 5;

      gl.uniform1f(u.time, elapsed * speed + params.offset * 0.01);
      gl.uniform2f(u.resolution, canvas.width, canvas.height);
      gl.uniform1f(u.pixelRatio, window.devicePixelRatio || 1);
      gl.uniform1f(u.scale, params.scale);
      gl.uniform1f(u.rotation, (params.rotation * Math.PI) / 180);

      var c1 = hexToRgba(params.color1);
      var c2 = hexToRgba(params.color2);
      var c3 = hexToRgba(params.color3);
      gl.uniform4f(u.color1, c1[0], c1[1], c1[2], c1[3]);
      gl.uniform4f(u.color2, c2[0], c2[1], c2[2], c2[3]);
      gl.uniform4f(u.color3, c3[0], c3[1], c3[2], c3[3]);

      gl.uniform1f(u.proportion, params.proportion / 100);
      gl.uniform1f(u.softness, params.softness / 100);
      gl.uniform1f(u.shape, PatternShapes[params.shape]);
      gl.uniform1f(u.shapeScale, params.shapeSize / 100);
      gl.uniform1f(u.distortion, params.distortion / 50);
      gl.uniform1f(u.swirl, params.swirl / 100);
      gl.uniform1f(u.swirlIterations, params.swirl === 0 ? 0 : params.swirlIterations);

      gl.drawArrays(gl.TRIANGLES, 0, 6);
      frameId = requestAnimationFrame(animate);
    }
    frameId = requestAnimationFrame(animate);

    // Pausa a animação quando a aba não está visível (economia de bateria/GPU).
    function onVisibility() {
      if (document.hidden) {
        running = false;
        if (frameId) cancelAnimationFrame(frameId);
      } else if (!running) {
        running = true;
        startTime = performance.now();
        frameId = requestAnimationFrame(animate);
      }
    }
    document.addEventListener("visibilitychange", onVisibility);

    return {
      destroy: function () {
        running = false;
        if (frameId) cancelAnimationFrame(frameId);
        document.removeEventListener("visibilitychange", onVisibility);
        resizeObserver.disconnect();
        gl.deleteProgram(program);
        gl.deleteShader(vertexShader);
        gl.deleteShader(fragmentShader);
        gl.deleteBuffer(positionBuffer);
        if (canvas.parentNode) canvas.parentNode.removeChild(canvas);
      },
    };
  }

  // Auto-init: qualquer elemento com [data-fs-gradient] recebe o gradiente.
  function autoInit() {
    var nodes = document.querySelectorAll("[data-fs-gradient]");
    for (var i = 0; i < nodes.length; i++) {
      var el = nodes[i];
      if (el.__fsGradientMounted) continue;
      el.__fsGradientMounted = true;
      var preset = el.getAttribute("data-preset") || "Prism";
      var speedAttr = el.getAttribute("data-speed");
      var cfg = { preset: preset };
      if (speedAttr != null) cfg.speed = parseFloat(speedAttr);
      mount(el, cfg);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", autoInit);
  } else {
    autoInit();
  }

  global.FSAnimatedGradient = { mount: mount, presets: presets };
})(window);
