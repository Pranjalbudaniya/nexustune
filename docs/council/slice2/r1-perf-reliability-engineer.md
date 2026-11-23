# Council Reconvene (Post-Slice 2) - Round 1: Performance / Reliability Engineer

## Assessment & Guardrails
1. Frame timing check: Slices 1 and 2 build cleanly in under 20 seconds.
2. Coil memory cache: Restrict bitmap pool to 25% of available JVM heap to avoid OOM on 1080x2392 screens.
3. Display mode negotiation: `negotiate120HzDisplayMode()` executed in `MainActivity` targets the 120Hz mode on device `10BF7Q150G000LE`.
4. Must maintain position ticking isolation during Slider updates.

