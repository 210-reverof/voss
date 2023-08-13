package com.yukgaejang.voss.domain.freeboard.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yukgaejang.voss.domain.freeboard.repository.entity.*;
import com.yukgaejang.voss.domain.freeboard.service.dto.response.MyPostListResponse;
import com.yukgaejang.voss.domain.freeboard.service.dto.response.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.yukgaejang.voss.domain.freeboard.repository.entity.QPost.post;
import static com.yukgaejang.voss.domain.freeboard.repository.entity.QPostComment.postComment;

@Repository
public class PostSupportRepositoryImpl implements PostSupportRepository {

    private final JPAQueryFactory jpaQueryFactory;

    static QPost p = QPost.post;
    static QPostComment pc = QPostComment.postComment;
    static QPostLike pl = QPostLike.postLike;
    static QPostFile pf = QPostFile.postFile;

    public PostSupportRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<PostListResponse> findAllByIsDeletedFalse(Pageable pageable) {
        List<PostListResponse> posts = jpaQueryFactory
                .selectDistinct(Projections.constructor(PostListResponse.class,
                        p,
                        JPAExpressions
                                .select(pc.id.count())
                                .from(pc)
                                .where(pc.post.id.eq(p.id)
                                        .and(pc.isDeleted.eq(0))),
                        JPAExpressions
                                .select(pl.id.count())
                                .from(pl)
                                .where(pl.post.id.eq(p.id)),
                        pf.contentType.like("image%"),
                        pf.contentType.notLike("image%")))
                .from(p)
                .leftJoin(p.member).fetchJoin()
                .leftJoin(pf).on(p.id.eq(pf.post.id).and(pf.isDeleted.eq(0))).fetchJoin()
                .where(p.isDeleted.eq(0))
                .orderBy(createOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(p.id)
                .from(p)
                .where(p.isDeleted.eq(0));

        return new PageImpl<>(posts, pageable, countQuery.fetchCount());
    }

    @Override
    public Page<PostListResponse> findAllByMemberNicknameAndIsDeletedFalse(Pageable pageable, String nickname) {
        List<PostListResponse> posts = jpaQueryFactory
                .selectDistinct(Projections.constructor(PostListResponse.class,
                        p,
                        JPAExpressions
                                .select(pc.id.count())
                                .from(pc)
                                .where(pc.post.id.eq(p.id)
                                        .and(pc.isDeleted.eq(0))),
                        JPAExpressions
                                .select(pl.id.count())
                                .from(pl)
                                .where(pl.post.id.eq(p.id)),
                        pf.contentType.like("image%"),
                        pf.contentType.notLike("image%")))
                .from(p)
                .leftJoin(p.member).fetchJoin()
                .leftJoin(pf).on(p.id.eq(pf.post.id).and(pf.isDeleted.eq(0))).fetchJoin()
                .where(
                    p.isDeleted.eq(0).and(p.member.nickname.eq(nickname))
                )
                .orderBy(createOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(p.id.count())
                .from(p)
                .where(p.isDeleted.eq(0).and(p.member.nickname.eq(nickname)));

        return new PageImpl<>(posts, pageable, countQuery.fetchOne());
    }

    @Override
    public Page<PostListResponse> findAllByTitleContainingAndIsDeletedFalse(Pageable pageable, String title) {
        List<PostListResponse> posts = jpaQueryFactory
                .selectDistinct(Projections.constructor(PostListResponse.class,
                        p,
                        JPAExpressions
                                .select(pc.id.count())
                                .from(pc)
                                .where(pc.post.id.eq(p.id)
                                        .and(pc.isDeleted.eq(0))),
                        JPAExpressions
                                .select(pl.id.count())
                                .from(pl)
                                .where(pl.post.id.eq(p.id)),
                        pf.contentType.like("image%"),
                        pf.contentType.notLike("image%")))
                .from(p)
                .leftJoin(p.member).fetchJoin()
                .leftJoin(pf).on(p.id.eq(pf.post.id).and(pf.isDeleted.eq(0))).fetchJoin()
                .where(
                        p.isDeleted.eq(0).and(p.title.contains(title))
                )
                .orderBy(createOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(p.id.count())
                .from(p)
                .where(p.isDeleted.eq(0).and(p.title.contains(title)));

        return new PageImpl<>(posts, pageable, countQuery.fetchOne());
    }

    @Override
    public Page<PostListResponse> findAllByContentContainingAndIsDeletedFalse(Pageable pageable, String content) {
        List<PostListResponse> posts = jpaQueryFactory
                .selectDistinct(Projections.constructor(PostListResponse.class,
                        p,
                        JPAExpressions
                                .select(pc.id.count())
                                .from(pc)
                                .where(pc.post.id.eq(p.id)
                                        .and(pc.isDeleted.eq(0))),
                        JPAExpressions
                                .select(pl.id.count())
                                .from(pl)
                                .where(pl.post.id.eq(p.id)),
                        pf.contentType.like("image%"),
                        pf.contentType.notLike("image%")))
                .from(p)
                .leftJoin(p.member).fetchJoin()
                .leftJoin(pf).on(p.id.eq(pf.post.id).and(pf.isDeleted.eq(0)))
                .where(
                        p.isDeleted.eq(0).and((p.content.contains(content)).or(p.title.contains(content)))
                )
                .orderBy(createOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(p.id.count())
                .from(p)
                .where(p.isDeleted.eq(0).and((p.content.contains(content)).or(p.title.contains(content))));

        return new PageImpl<>(posts, pageable, countQuery.fetchOne());
    }


    @Override
    public Post findByIdAndIsDeletedFalse(Long id) {
        return jpaQueryFactory
                .selectFrom(post)
                .where(post.id.eq(id).and(post.isDeleted.eq(0)))
                .fetchOne();
    }

    @Override
    public Page<MyPostListResponse> findAllByMemberEmailAndIsDeletedFalse(Pageable pageable, String email) {
        List<MyPostListResponse> posts = jpaQueryFactory
                .selectDistinct(Projections.constructor(MyPostListResponse.class,
                        p,
                        JPAExpressions
                                .select(pc.id.count())
                                .from(pc)
                                .where(pc.post.id.eq(p.id)
                                        .and(pc.isDeleted.eq(0))),
                        JPAExpressions
                                .select(pl.id.count())
                                .from(pl)
                                .where(pl.post.id.eq(p.id)),
                        pf.contentType.like("image%"),
                        pf.contentType.notLike("image%")))
                .from(p)
                .leftJoin(p.member).fetchJoin()
                .leftJoin(pc).on(p.id.eq(pc.post.id).and(pc.isDeleted.eq(0))).fetchJoin()
                .leftJoin(pl).on(p.id.eq(pl.post.id)).fetchJoin()
                .leftJoin(pf).on(p.id.eq(pf.post.id).and(pf.isDeleted.eq(0))).fetchJoin()
                .where(
                        p.isDeleted.eq(0).and(p.member.email.eq(email))
                )
                .groupBy(p.id)
                .orderBy(p.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(p.id.count())
                .from(p)
                .where(p.isDeleted.eq(0).and(p.member.email.eq(email)));

        return new PageImpl<>(posts, pageable, countQuery.fetchOne());
    }

    private OrderSpecifier[] createOrderSpecifier(Pageable pageable) {
        OrderSpecifier[] orderSpecifiers = pageable.getSort().stream().map(order -> {
            if(order.getProperty().equals("hit")) {
                return p.hit.desc();
            } else if(order.getProperty().equals("like")) {
                return pl.id.count().desc();
            } else {
                return p.createdAt.desc();
            }
        }).toArray(OrderSpecifier[]::new);

        return orderSpecifiers;
    }

}