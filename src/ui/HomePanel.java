/*
 * Concepts: Swing UI, Observer Pattern, Stream API usage via BookingManager
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Bookable;
import model.Concert;
import model.SportEvent;
import service.BookingManager;
import service.SeatObserver;

public class HomePanel extends JPanel implements SeatObserver {
    public interface BookNowListener {
        void onBookNow(Bookable event);
        void onImageClick(Bookable event);
    }

    private final BookingManager manager;
    private final BookNowListener listener;
    private final JPanel sectionsPanel = new JPanel();
    private String selectedCategory = "All";
    private final JPanel categoryBar = new JPanel();
    private String searchQuery = "";
    private final Map<String, Image> imageCache = new HashMap<String, Image>();
    private JScrollPane mainScroll;

    public HomePanel(BookingManager manager, BookNowListener listener) {
        this.manager = manager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);

        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x333545),
                        getWidth(), getHeight(), new Color(0xF84464));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hero.setPreferredSize(new Dimension(0, 150));
        hero.setLayout(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel headline = new JLabel("Discover events near you");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = new JLabel("Movies, live shows, stadium matches and more");
        sub.setForeground(new Color(0xe5e7eb));
        sub.setFont(UITheme.BODY);

        JPanel heroText = new JPanel(new BorderLayout());
        heroText.setOpaque(false);
        heroText.add(headline, BorderLayout.NORTH);
        heroText.add(sub, BorderLayout.SOUTH);
        hero.add(heroText, BorderLayout.WEST);

        categoryBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        categoryBar.setBackground(UITheme.BG);
        categoryBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        buildCategoryButtons();

        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setBackground(UITheme.BG);

        mainScroll = new JScrollPane(sectionsPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(UITheme.BG);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        mainScroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG);
        center.add(categoryBar, BorderLayout.NORTH);
        center.add(mainScroll, BorderLayout.CENTER);

        add(hero, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshCards();
    }

    public void refreshCards() {
        sectionsPanel.removeAll();

        List<Bookable> all = manager.getAllEvents();
        List<Bookable> trending = filterByCategory(all, selectedCategory);
        List<Bookable> movies = filterByCategory(all, "Movies");
        List<Bookable> live = filterByCategory(all, "Concerts");
        live.addAll(filterByCategory(all, "Standups"));
        List<Bookable> sports = filterByCategory(all, "Sports");

        sectionsPanel.add(createSection("Trending in " + selectedCategory, trending));
        sectionsPanel.add(createSection("Recommended Movies", movies));
        sectionsPanel.add(createSection("Best of Live Events", live));
        sectionsPanel.add(createSection("Sports Events", sports));

        sectionsPanel.revalidate();
        sectionsPanel.repaint();
    }

    private void buildCategoryButtons() {
        categoryBar.removeAll();
        String[] categories = new String[] { "All", "Movies", "Concerts", "Sports", "Standups" };
        for (final String cat : categories) {
            JButton chip = new JButton(cat);
            chip.setFocusPainted(false);
            chip.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            if (cat.equals(selectedCategory)) {
                chip.setBackground(UITheme.INDIGO);
                chip.setForeground(Color.WHITE);
            } else {
                chip.setBackground(Color.WHITE);
                chip.setForeground(UITheme.MUTED);
                chip.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
            }
            chip.addActionListener(e -> {
                selectedCategory = cat;
                buildCategoryButtons();
                refreshCards();
            });
            categoryBar.add(chip);
        }
        categoryBar.revalidate();
        categoryBar.repaint();
    }

    private JPanel createSection(String title, List<Bookable> events) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(UITheme.BG);
        section.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.SUBTITLE);
        JLabel seeAll = new JLabel("See All");
        seeAll.setForeground(UITheme.INDIGO);
        seeAll.setFont(UITheme.BODY);
        header.add(titleLabel, BorderLayout.WEST);
        header.add(seeAll, BorderLayout.EAST);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        row.setBackground(UITheme.BG);

        for (Bookable event : events) {
            row.add(createCard(event));
        }

        JScrollPane scroller = new JScrollPane(row);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroller.getViewport().setBackground(UITheme.BG);
        scroller.setPreferredSize(new Dimension(0, 250));
        scroller.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroller.getHorizontalScrollBar().setUnitIncrement(18);
        scroller.addMouseWheelListener(e -> {
            if (e.isShiftDown()) {
                int delta = e.getWheelRotation() * 30;
                int val = scroller.getHorizontalScrollBar().getValue();
                scroller.getHorizontalScrollBar().setValue(val + delta);
            } else if (mainScroll != null) {
                int delta = e.getWheelRotation() * 30;
                int val = mainScroll.getVerticalScrollBar().getValue();
                mainScroll.getVerticalScrollBar().setValue(val + delta);
            }
        });

        section.add(header, BorderLayout.NORTH);
        section.add(scroller, BorderLayout.CENTER);
        return section;
    }

    private JPanel createCard(final Bookable event) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(170, 220));
        card.setBackground(UITheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));

        JPanel image = new ImagePanel(event);
        image.setPreferredSize(new Dimension(170, 130));
        image.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                listener.onImageClick(event);
            }
        });

        JLabel name = new JLabel(event.getName());
        name.setFont(UITheme.BODY);
        name.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));

        JLabel meta = new JLabel(event.getDate() + " | " + event.getVenueName());
        meta.setFont(UITheme.BODY);
        meta.setForeground(UITheme.MUTED);
        meta.setBorder(BorderFactory.createEmptyBorder(2, 8, 0, 8));

        JLabel price = new JLabel("From Rs " + (int) event.getPrice());
        price.setFont(UITheme.BODY);
        price.setForeground(UITheme.INDIGO);
        price.setBorder(BorderFactory.createEmptyBorder(2, 8, 6, 8));

        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(UITheme.CARD);
        JPanel text = new JPanel(new BorderLayout());
        text.setBackground(UITheme.CARD);
        text.add(name, BorderLayout.NORTH);
        text.add(meta, BorderLayout.CENTER);
        text.add(price, BorderLayout.SOUTH);
        info.add(text, BorderLayout.CENTER);

        card.add(image, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    public boolean applySearch(String query) {
        searchQuery = query == null ? "" : query.trim().toLowerCase();
        boolean found = false;
        for (Bookable e : manager.getAllEvents()) {
            if (matchesSearch(e)) {
                found = true;
                break;
            }
        }
        refreshCards();
        return found || searchQuery.isEmpty();
    }

    private List<Bookable> filterByCategory(List<Bookable> all, String category) {
        List<Bookable> list = new ArrayList<Bookable>();
        for (Bookable e : all) {
            if (matchesCategory(e, category) && matchesSearch(e)) {
                list.add(e);
            }
        }
        return list;
    }

    private boolean matchesCategory(Bookable event, String category) {
        if ("All".equals(category)) return true;
        String cat = getCategory(event);
        return category.equals(cat);
    }

    private String getCategory(Bookable event) {
        String name = event.getName().toLowerCase();
        if (name.contains("standup")) return "Standups";
        if (event instanceof Concert) return "Concerts";
        if (event instanceof SportEvent) return "Sports";
        return "Movies";
    }

    private boolean matchesSearch(Bookable event) {
        if (searchQuery == null || searchQuery.isEmpty()) return true;
        String q = searchQuery.toLowerCase();
        return event.getName().toLowerCase().contains(q) ||
                event.getVenueName().toLowerCase().contains(q) ||
                getCategory(event).toLowerCase().contains(q);
    }

    private class ImagePanel extends JPanel {
        private final Bookable event;

        public ImagePanel(Bookable event) {
            this.event = event;
            setBackground(new Color(0x111827));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Image img = getImageForEvent(event);
            if (img != null) {
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, getHeight() - 26, getWidth(), 26);
                g2.setColor(Color.WHITE);
                g2.setFont(UITheme.BODY);
                g2.drawString(event.getName(), 8, getHeight() - 8);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x111827), getWidth(), getHeight(), new Color(0xF84464));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(UITheme.BODY);
                g2.drawString(event.getName(), 10, getHeight() / 2 + 4);
            }
        }
    }

    private Image getImageForEvent(Bookable event) {
        String category = getCategory(event);
        if (imageCache.containsKey(category)) {
            return imageCache.get(category);
        }
        String path = null;
        if ("Movies".equals(category)) path = "assets/movie.jpg";
        if ("Concerts".equals(category)) path = "assets/concert.jpg";
        if ("Standups".equals(category)) path = "assets/standup.jpg";
        if ("Sports".equals(category)) path = "assets/sports.jpg";
        if (path != null) {
            try {
                Image img = ImageIO.read(new File(path));
                imageCache.put(category, img);
                return img;
            } catch (Exception e) {
                imageCache.put(category, null);
            }
        }
        return null;
    }

    @Override
    public void onSeatAvailable(Bookable event) {
        JOptionPane.showMessageDialog(this, "Seat opened for: " + event.getName());
        refreshCards();
    }
}
