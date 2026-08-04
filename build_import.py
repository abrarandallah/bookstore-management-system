import json

books = [
    ("The Art of Unfinished Things", "Laila Morningside", [
        ("Start Before You're Ready", "Progress begins when you stop waiting for perfect conditions."),
        ("Celebrate Incompletion", "Unfinished projects hold lessons just as valuable as finished ones."),
        ("Momentum Over Perfection", "Small steps compound faster than flawless plans."),
        ("Let Go of Control", "Creativity thrives when you allow space for uncertainty."),
        ("Redefine Success", "Success can mean growth, not completion."),
    ]),
    ("Why We Forget", "Dr. Elias Rowan", [
        ("Memory is Selective", "Forgetting is the brain's way of prioritizing."),
        ("Stress Blocks Recall", "Anxiety clouds access to stored memories."),
        ("Sleep Restores Memory", "Deep rest strengthens recall and learning."),
        ("Emotions Anchor Memories", "Strong feelings make events unforgettable."),
        ("Practice Builds Retention", "Repetition is the antidote to forgetting."),
    ]),
    ("The Invisible Rules of Conversation", "Mira Chen", [
        ("Listen Twice, Speak Once", "Active listening builds trust."),
        ("Silence is Powerful", "Pauses give weight to words."),
        ("Tone Shapes Meaning", "How you say it matters more than what you say."),
        ("Questions Unlock Depth", "Curiosity keeps dialogue alive."),
        ("Respect the Flow", "Conversations are dances, not debates."),
    ]),
    ("How to Travel Without Moving", "Jonah Vale", [
        ("Books as Journeys", "Reading transports you across cultures."),
        ("Food as Geography", "Cooking lets you taste distant lands."),
        ("Music as Maps", "Songs carry the spirit of places."),
        ("Imagination as Flight", "Daydreaming is the cheapest ticket."),
        ("Perspective as Passport", "Seeing through others' eyes expands horizons."),
    ]),
    ("The Mathematics of Happiness", "Sofia Delgado", [
        ("Subtract Negativity", "Removing toxic influences multiplies joy."),
        ("Add Gratitude", "Daily appreciation compounds emotional wealth."),
        ("Divide Time Wisely", "Balance between work and play sustains happiness."),
        ("Multiply Kindness", "Acts of generosity ripple outward."),
        ("Equations of Meaning", "Happiness is solved through purpose, not possessions."),
    ]),
    ("Echoes of Tomorrow", "Daniel Stryker", [
        ("Dual Futures", "Every choice creates two timelines: one of hope, one of regret."),
        ("Technology Mirrors Humanity", "Progress reflects our deepest fears and desires."),
        ("Memory as Architecture", "The past builds the scaffolding of tomorrow."),
        ("Echoes Never Fade", "Actions ripple through time, shaping unseen destinies."),
        ("Hope in Ruins", "Even dystopias hold seeds of renewal."),
    ]),
    ("The Cartographer of Dreams", "Julian Northwood", [
        ("Mapping the Mind", "Imagination is the geography of the soul."),
        ("Dreams as Coordinates", "Each dream points to a hidden truth."),
        ("The Compass Within", "Intuition guides where logic cannot."),
        ("Lost Islands of Memory", "Forgotten dreams hold forgotten wisdom."),
        ("Draw Your Own Map", "The journey matters more than the destination."),
    ]),
    ("The Physics of Loneliness", "Daniel Kessler", [
        ("Gravity of Solitude", "Loneliness pulls us inward to rediscover ourselves."),
        ("Entropy of Connection", "Relationships decay without energy and care."),
        ("Quantum Emotions", "Feelings exist in multiple states until expressed."),
        ("Light in the Void", "Even isolation can illuminate understanding."),
        ("Universal Constants", "We all orbit the same need for belonging."),
    ]),
    ("The Library of Lost Sounds", "Alec Hartman", [
        ("Silence Has Memory", "Every sound leaves an echo in time."),
        ("Music as History", "Melodies preserve emotions better than words."),
        ("Listening to Absence", "What's missing teaches more than what's heard."),
        ("Resonance of the Soul", "Sound connects us to something eternal."),
        ("Reclaim the Noise", "Rediscover the forgotten symphonies of life."),
    ]),
    ("The Algorithm of Emotion", "Daniel Kepler", [
        ("Data Meets Heart", "Emotions can be decoded but never fully defined."),
        ("Human Variables", "Love, fear, and empathy resist computation."),
        ("Artificial Empathy", "Machines can mimic feeling but not meaning."),
        ("Code of Connection", "True intelligence lies in compassion."),
        ("Emotional Logic", "Understanding others begins with understanding yourself."),
    ]),
]

result = []
for name, author, takeaways in books:
    result.append({
        "name": name,
        "author": author,
        "takeaways": [{"heading": h, "content": c} for h, c in takeaways]
    })

with open("books-import.json", "w") as f:
    json.dump(result, f, indent=2, ensure_ascii=False)

print(f"Wrote {len(result)} books")
